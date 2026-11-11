package ru.student.volunteer_events;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.student.volunteer_events.domain.entity.Application;
import ru.student.volunteer_events.domain.entity.Event;
import ru.student.volunteer_events.domain.entity.Skill;
import ru.student.volunteer_events.domain.entity.User;
import ru.student.volunteer_events.domain.repository.ApplicationRepository;
import ru.student.volunteer_events.domain.repository.EventRepository;
import ru.student.volunteer_events.dto.ApplicationResponse;
import ru.student.volunteer_events.enums.ApplicationStatus;
import ru.student.volunteer_events.enums.EventStatus;
import ru.student.volunteer_events.enums.UserRole;
import ru.student.volunteer_events.exception.BusinessException;
import ru.student.volunteer_events.service.ApplicationService;
import ru.student.volunteer_events.service.EventService;
import ru.student.volunteer_events.service.SkillAndUserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventService eventService;
    @Mock
    private SkillAndUserService userService;

    @InjectMocks
    private ApplicationService applicationService;

    @Test
    void apply_WhenMissingRequiredSkill_ShouldThrowBusinessException() {
        Skill medSkill = Skill.builder().id(1L).name("Первая помощь").build();
        User volunteer = User.builder().id(2L).username("vol1").role(UserRole.VOLUNTEER).skills(Set.of()).build();
        Event event = Event.builder()
                .id(10L)
                .status(EventStatus.OPEN)
                .requiredSkills(Set.of(medSkill))
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(3))
                .maxParticipants(5)
                .build();

        when(userService.getUserByUsername("vol1")).thenReturn(volunteer);
        when(eventService.findEventEntity(10L)).thenReturn(event);
        when(applicationRepository.existsByVolunteerIdAndEventIdAndStatusIn(any(), any(), any())).thenReturn(false);

        assertThrows(BusinessException.class, () -> applicationService.apply(10L, "vol1"));
    }

    @Test
    void apply_WhenValid_ShouldCreatePendingApplication() {
        User volunteer = User.builder().id(2L).username("vol1").fullName("Анна").role(UserRole.VOLUNTEER).skills(Set.of()).build();
        Event event = Event.builder()
                .id(10L)
                .title("Субботник")
                .status(EventStatus.OPEN)
                .requiredSkills(Set.of())
                .startTime(LocalDateTime.now().plusDays(2))
                .endTime(LocalDateTime.now().plusDays(2).plusHours(4))
                .maxParticipants(10)
                .build();

        when(userService.getUserByUsername("vol1")).thenReturn(volunteer);
        when(eventService.findEventEntity(10L)).thenReturn(event);
        when(applicationRepository.existsByVolunteerIdAndEventIdAndStatusIn(any(), any(), any())).thenReturn(false);
        when(applicationRepository.hasOverlappingApprovedEvent(any(), any(), any(), any())).thenReturn(false);
        when(applicationRepository.countByEventIdAndStatusIn(any(), any())).thenReturn(0L);
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> {
            Application a = inv.getArgument(0);
            a.setId(100L);
            return a;
        });

        ApplicationResponse response = applicationService.apply(10L, "vol1");
        assertEquals(ApplicationStatus.PENDING, response.getStatus());
        assertEquals("Субботник", response.getEventTitle());
    }

    @Test
    void markAttended_ShouldCreditHoursFromEventDuration() {
        User coord = User.builder().id(1L).username("coord1").role(UserRole.COORDINATOR).build();
        User vol = User.builder().id(2L).username("vol1").fullName("Анна").role(UserRole.VOLUNTEER).build();
        Event event = Event.builder()
                .id(10L)
                .title("Забег")
                .coordinator(coord)
                .startTime(LocalDateTime.of(2026, 12, 1, 10, 0))
                .endTime(LocalDateTime.of(2026, 12, 1, 14, 0))
                .build();
        Application app = Application.builder()
                .id(5L)
                .event(event)
                .volunteer(vol)
                .status(ApplicationStatus.APPROVED)
                .build();

        when(applicationRepository.findById(5L)).thenReturn(Optional.of(app));
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        ApplicationResponse res = applicationService.markAttended(5L, null, "coord1");
        assertEquals(ApplicationStatus.ATTENDED, res.getStatus());
        assertEquals(4.0, res.getCreditedHours());
    }
}
