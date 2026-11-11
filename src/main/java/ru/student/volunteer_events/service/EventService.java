package ru.student.volunteer_events.service;

import jakarta.persistence.criteria.Join;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.student.volunteer_events.domain.entity.Event;
import ru.student.volunteer_events.domain.entity.Skill;
import ru.student.volunteer_events.domain.entity.User;
import ru.student.volunteer_events.domain.repository.ApplicationRepository;
import ru.student.volunteer_events.domain.repository.EventRepository;
import ru.student.volunteer_events.domain.repository.SkillRepository;
import ru.student.volunteer_events.dto.EventRequest;
import ru.student.volunteer_events.dto.EventResponse;
import ru.student.volunteer_events.enums.ApplicationStatus;
import ru.student.volunteer_events.enums.EventStatus;
import ru.student.volunteer_events.exception.AccessDeniedCustomException;
import ru.student.volunteer_events.exception.BusinessException;
import ru.student.volunteer_events.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final SkillRepository skillRepository;
    private final ApplicationRepository applicationRepository;
    private final SkillAndUserService userService;

    @Transactional(readOnly = true)
    public Page<EventResponse> searchEvents(
            EventStatus status,
            String location,
            Long skillId,
            LocalDateTime from,
            Pageable pageable
    ) {
        Specification<Event> spec = Specification.where(null);

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (location != null && !location.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%"));
        }
        if (from != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startTime"), from));
        }
        if (skillId != null) {
            spec = spec.and((root, query, cb) -> {
                Join<Event, Skill> skillsJoin = root.join("requiredSkills");
                return cb.equal(skillsJoin.get("id"), skillId);
            });
        }

        return eventRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public EventResponse getById(Long id) {
        return toResponse(findEventEntity(id));
    }

    @Transactional
    public EventResponse createEvent(EventRequest request, String coordinatorUsername) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BusinessException("Дата окончания должна быть позже даты начала");
        }

        User coordinator = userService.getUserByUsername(coordinatorUsername);
        Set<Skill> skills = resolveSkills(request.getRequiredSkillIds());

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .maxParticipants(request.getMaxParticipants())
                .status(EventStatus.PLANNED)
                .coordinator(coordinator)
                .requiredSkills(skills)
                .build();

        return toResponse(eventRepository.save(event));
    }

    @Transactional
    public EventResponse updateEvent(Long id, EventRequest request, String coordinatorUsername) {
        Event event = findEventEntity(id);
        checkOwner(event, coordinatorUsername);

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BusinessException("Дата окончания должна быть позже даты начала");
        }

        long approved = applicationRepository.countByEventIdAndStatusIn(id, List.of(ApplicationStatus.APPROVED, ApplicationStatus.ATTENDED));
        if (request.getMaxParticipants() < approved) {
            throw new BusinessException("Новый лимит мест меньше количества уже одобренных участников (" + approved + ")");
        }

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setLocation(request.getLocation());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setMaxParticipants(request.getMaxParticipants());
        event.setRequiredSkills(resolveSkills(request.getRequiredSkillIds()));

        return toResponse(eventRepository.save(event));
    }

    @Transactional
    public EventResponse changeStatus(Long id, EventStatus newStatus, String coordinatorUsername) {
        Event event = findEventEntity(id);
        checkOwner(event, coordinatorUsername);

        if (event.getStatus() == EventStatus.FINISHED || event.getStatus() == EventStatus.CANCELLED) {
            throw new BusinessException("Нельзя изменить статус уже завершённого или отменённого мероприятия");
        }

        event.setStatus(newStatus);
        return toResponse(eventRepository.save(event));
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void autoFinishExpiredEvents() {
        List<Event> expired = eventRepository.findByStatusAndEndTimeBefore(EventStatus.OPEN, LocalDateTime.now());
        for (Event event : expired) {
            event.setStatus(EventStatus.FINISHED);
        }
        eventRepository.saveAll(expired);
    }

    public Event findEventEntity(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Мероприятие с ID " + id + " не найдено"));
    }

    public void checkOwner(Event event, String coordinatorUsername) {
        if (!event.getCoordinator().getUsername().equals(coordinatorUsername)) {
            throw new AccessDeniedCustomException("Вы не являетесь организатором этого мероприятия");
        }
    }

    private Set<Skill> resolveSkills(Set<Long> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Skill> found = skillRepository.findAllById(skillIds);
        if (found.size() != skillIds.size()) {
            throw new NotFoundException("Некоторые из указанных навыков не найдены");
        }
        return new HashSet<>(found);
    }

    public EventResponse toResponse(Event event) {
        long approvedCount = applicationRepository.countByEventIdAndStatusIn(
                event.getId(),
                List.of(ApplicationStatus.APPROVED, ApplicationStatus.ATTENDED)
        );
        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .location(event.getLocation())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .maxParticipants(event.getMaxParticipants())
                .approvedCount(approvedCount)
                .status(event.getStatus())
                .coordinatorName(event.getCoordinator().getFullName())
                .requiredSkills(event.getRequiredSkills().stream().map(Skill::getName).collect(Collectors.toSet()))
                .build();
    }
}
