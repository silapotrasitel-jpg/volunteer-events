package ru.student.volunteer_events.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.student.volunteer_events.enums.UserRole;

@Data
public class RegisterRequest {
    @NotBlank(message = "Логин обязателен")
    @Size(min = 3, max = 50, message = "Логин от 3 до 50 символов")
    private String username;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 4, message = "Пароль минимум 4 символа")
    private String password;

    @NotBlank(message = "ФИО обязательно")
    private String fullName;

    private String phone;

    @NotNull(message = "Роль обязательна (VOLUNTEER или COORDINATOR)")
    private UserRole role;
}
