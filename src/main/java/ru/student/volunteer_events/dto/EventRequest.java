package ru.student.volunteer_events.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class EventRequest {
    @NotBlank(message = "Название мероприятия обязательно")
    private String title;

    private String description;

    @NotBlank(message = "Место проведения обязательно")
    private String location;

    @NotNull(message = "Дата начала обязательна")
    @Future(message = "Дата начала должна быть в будущем")
    private LocalDateTime startTime;

    @NotNull(message = "Дата окончания обязательна")
    private LocalDateTime endTime;

    @NotNull(message = "Лимит участников обязателен")
    @Min(value = 1, message = "Минимум 1 участник")
    private Integer maxParticipants;

    private Set<Long> requiredSkillIds;
}
