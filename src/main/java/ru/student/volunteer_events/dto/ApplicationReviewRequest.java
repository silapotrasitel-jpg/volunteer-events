package ru.student.volunteer_events.dto;

import lombok.Data;

@Data
public class ApplicationReviewRequest {
    private String comment;
    private Double creditedHours;
}
