package ru.student.volunteer_events.dto;

import lombok.Builder;
import lombok.Data;
import ru.student.volunteer_events.enums.ApplicationStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class ApplicationResponse {
    private Long id;
    private Long eventId;
    private String eventTitle;
    private Long volunteerId;
    private String volunteerName;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;
    private String coordinatorComment;
    private Double creditedHours;
}
