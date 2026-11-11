package ru.student.volunteer_events.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.student.volunteer_events.domain.entity.Event;
import ru.student.volunteer_events.enums.EventStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    List<Event> findByStatusAndEndTimeBefore(EventStatus status, LocalDateTime time);
}
