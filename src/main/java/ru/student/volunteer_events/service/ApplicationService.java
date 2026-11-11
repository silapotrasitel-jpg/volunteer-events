package ru.student.volunteer_events.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.student.volunteer_events.domain.entity.Application;
import ru.student.volunteer_events.domain.entity.Event;
import ru.student.volunteer_events.domain.entity.User;
import ru.student.volunteer_events.domain.repository.ApplicationRepository;
import ru.student.volunteer_events.domain.repository.EventRepository;
import ru.student.volunteer_events.dto.ApplicationResponse;
import ru.student.volunteer_events.dto.ApplicationReviewRequest;
import ru.student.volunteer_events.enums.ApplicationStatus;
import ru.student.volunteer_events.enums.EventStatus;
import ru.student.volunteer_events.exception.AccessDeniedCustomException;
import ru.student.volunteer_events.exception.BusinessException;
import ru.student.volunteer_events.exception.NotFoundException;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final EventRepository eventRepository;
    private final EventService eventService;
    private final SkillAndUserService userService;

    @Transactional
    public ApplicationResponse apply(Long eventId, String volunteerUsername) {
        User volunteer = userService.getUserByUsername(volunteerUsername);
        Event event = eventService.findEventEntity(eventId);

        if (event.getStatus() != EventStatus.OPEN) {
            throw new BusinessException("Подать заявку можно только на открытое (OPEN) мероприятие");
        }

        boolean alreadyApplied = applicationRepository.existsByVolunteerIdAndEventIdAndStatusIn(
                volunteer.getId(),
                eventId,
                List.of(ApplicationStatus.PENDING, ApplicationStatus.APPROVED, ApplicationStatus.ATTENDED)
        );
        if (alreadyApplied) {
            throw new BusinessException("Вы уже подали активную заявку на это мероприятие");
        }

        if (!event.getRequiredSkills().isEmpty() && !volunteer.getSkills().containsAll(event.getRequiredSkills())) {
            throw new BusinessException("У вас отсутствуют обязательные навыки для этого мероприятия");
        }

        boolean hasOverlap = applicationRepository.hasOverlappingApprovedEvent(
                volunteer.getId(),
                eventId,
                event.getStartTime(),
                event.getEndTime()
        );
        if (hasOverlap) {
            throw new BusinessException("У вас уже есть одобренное мероприятие, пересекающееся по времени");
        }

        long currentApproved = applicationRepository.countByEventIdAndStatusIn(
                eventId,
                List.of(ApplicationStatus.APPROVED, ApplicationStatus.ATTENDED)
        );
        if (currentApproved >= event.getMaxParticipants()) {
            throw new BusinessException("Свободных мест на мероприятие больше нет");
        }

        Application application = Application.builder()
                .volunteer(volunteer)
                .event(event)
                .status(ApplicationStatus.PENDING)
                .appliedAt(LocalDateTime.now())
                .creditedHours(0.0)
                .build();

        return toResponse(applicationRepository.save(application));
    }

    @Transactional
    public ApplicationResponse cancelByVolunteer(Long appId, String volunteerUsername) {
        Application app = findAppEntity(appId);
        if (!app.getVolunteer().getUsername().equals(volunteerUsername)) {
            throw new AccessDeniedCustomException("Вы можете отменить только свою заявку");
        }
        if (app.getStatus() != ApplicationStatus.PENDING && app.getStatus() != ApplicationStatus.APPROVED) {
            throw new BusinessException("Нельзя отменить заявку в текущем статусе: " + app.getStatus());
        }

        Event event = app.getEvent();
        boolean wasApproved = app.getStatus() == ApplicationStatus.APPROVED;

        app.setStatus(ApplicationStatus.CANCELLED);
        applicationRepository.save(app);

        if (wasApproved && event.getStatus() == EventStatus.CLOSED) {
            long approvedNow = applicationRepository.countByEventIdAndStatusIn(
                    event.getId(),
                    List.of(ApplicationStatus.APPROVED, ApplicationStatus.ATTENDED)
            );
            if (approvedNow < event.getMaxParticipants()) {
                event.setStatus(EventStatus.OPEN);
                eventRepository.save(event);
            }
        }

        return toResponse(app);
    }
    @Transactional
    public ApplicationResponse approve(Long appId, ApplicationReviewRequest request, String coordinatorUsername) {
        Application app = findAppEntity(appId);
        Event event = app.getEvent();
        eventService.checkOwner(event, coordinatorUsername);

        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new BusinessException("Одобрить можно только заявку в статусе PENDING");
        }

        long currentApproved = applicationRepository.countByEventIdAndStatusIn(
                event.getId(),
                List.of(ApplicationStatus.APPROVED, ApplicationStatus.ATTENDED)
        );
        if (currentApproved >= event.getMaxParticipants()) {
            throw new BusinessException("Достигнут лимит участников мероприятия");
        }

        boolean hasOverlap = applicationRepository.hasOverlappingApprovedEvent(
                app.getVolunteer().getId(),
                event.getId(),
                event.getStartTime(),
                event.getEndTime()
        );
        if (hasOverlap) {
            throw new BusinessException("У волонтёра появилось другое одобренное мероприятие на это время");
        }

        app.setStatus(ApplicationStatus.APPROVED);
        if (request != null && request.getComment() != null) {
            app.setCoordinatorComment(request.getComment());
        }
        applicationRepository.save(app);

        if (currentApproved + 1 >= event.getMaxParticipants() && event.getStatus() == EventStatus.OPEN) {
            event.setStatus(EventStatus.CLOSED);
            eventRepository.save(event);
        }

        return toResponse(app);
    }

    @Transactional
    public ApplicationResponse reject(Long appId, ApplicationReviewRequest request, String coordinatorUsername) {
        Application app = findAppEntity(appId);
        eventService.checkOwner(app.getEvent(), coordinatorUsername);

        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new BusinessException("Отклонить можно только заявку в статусе PENDING");
        }

        app.setStatus(ApplicationStatus.REJECTED);
        if (request != null && request.getComment() != null) {
            app.setCoordinatorComment(request.getComment());
        }
        return toResponse(applicationRepository.save(app));
    }

    @Transactional
    public ApplicationResponse markAttended(Long appId, ApplicationReviewRequest request, String coordinatorUsername) {
        Application app = findAppEntity(appId);
        Event event = app.getEvent();
        eventService.checkOwner(event, coordinatorUsername);

        if (app.getStatus() != ApplicationStatus.APPROVED) {
            throw new BusinessException("Подтвердить участие можно только для одобренной (APPROVED) заявки");
        }

        double defaultHours = Math.max(1.0, Duration.between(event.getStartTime(), event.getEndTime()).toMinutes() / 60.0);
        double hours = (request != null && request.getCreditedHours() != null && request.getCreditedHours() > 0)
                ? request.getCreditedHours()
                : defaultHours;

        app.setStatus(ApplicationStatus.ATTENDED);
        app.setCreditedHours(hours);
        if (request != null && request.getComment() != null) {
            app.setCoordinatorComment(request.getComment());
        }

        return toResponse(applicationRepository.save(app));
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getMyApplications(String volunteerUsername) {
        User volunteer = userService.getUserByUsername(volunteerUsername);
        return applicationRepository.findByVolunteerId(volunteer.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getEventApplications(Long eventId, String coordinatorUsername) {
        Event event = eventService.findEventEntity(eventId);
        eventService.checkOwner(event, coordinatorUsername);
        return applicationRepository.findByEventId(eventId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public byte[] exportParticipantsCsv(Long eventId, String coordinatorUsername) {
        Event event = eventService.findEventEntity(eventId);
        eventService.checkOwner(event, coordinatorUsername);

        List<Application> apps = applicationRepository.findByEventId(eventId);
        StringBuilder sb = new StringBuilder();
        sb.append("ID;ФИО волонтёра;Телефон;Статус заявки;Начислено часов;Комментарий\n");
        for (Application a : apps) {
            sb.append(a.getId()).append(";")
                    .append(a.getVolunteer().getFullName()).append(";")
                    .append(a.getVolunteer().getPhone() != null ? a.getVolunteer().getPhone() : "-").append(";")
                    .append(a.getStatus()).append(";")
                    .append(a.getCreditedHours()).append(";")
                    .append(a.getCoordinatorComment() != null ? a.getCoordinatorComment() : "")
                    .append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private Application findAppEntity(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Заявка с ID " + id + " не найдена"));
    }

    private ApplicationResponse toResponse(Application app) {
        return ApplicationResponse.builder()
                .id(app.getId())
                .eventId(app.getEvent().getId())
                .eventTitle(app.getEvent().getTitle())
                .volunteerId(app.getVolunteer().getId())
                .volunteerName(app.getVolunteer().getFullName())
                .status(app.getStatus())
                .appliedAt(app.getAppliedAt())
                .coordinatorComment(app.getCoordinatorComment())
                .creditedHours(app.getCreditedHours())
                .build();
    }
}
