package ru.student.volunteer_events.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SkillRequest {
    @NotBlank(message = "Название навыка не может быть пустым")
    private String name;
}
