package ru.student.volunteer_events.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Введите логин")
    private String username;

    @NotBlank(message = "Введите пароль")
    private String password;
}
