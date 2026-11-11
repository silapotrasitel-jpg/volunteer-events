package ru.student.volunteer_events.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.student.volunteer_events.domain.entity.Application;
import ru.student.volunteer_events.enums.ApplicationStatus;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByEventId(Long eventId);

    List<Application> findByVolunteerId(Long volunteerId);

    long countByEventIdAndStatusIn(Long eventId, Collection<ApplicationStatus> statuses);

    boolean existsByVolunteerIdAndEventIdAndStatusIn(Long volunteerId, Long eventId, Collection<ApplicationStatus> statuses);

    @Query("""
            SELECT COUNT(a) > 0 FROM Application a
            WHERE a.volunteer.id = :volunteerId
              AND a.status = 'APPROVED'
              AND a.event.id <> :eventId
              AND a.event.startTime < :endTime
              AND a.event.endTime > :startTime
            """)
    boolean hasOverlappingApprovedEvent(
            @Param("volunteerId") Long volunteerId,
            @Param("eventId") Long eventId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
