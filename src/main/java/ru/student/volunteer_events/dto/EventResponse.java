package ru.student.volunteer_events.dto;

import lombok.Builder;
import lombok.Data;
import ru.student.volunteer_events.enums.EventStatus;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer maxParticipants;
    private Long approvedCount;
    private EventStatus status;
    private String coordinatorName;
    private Set<String> requiredSkills;
}
