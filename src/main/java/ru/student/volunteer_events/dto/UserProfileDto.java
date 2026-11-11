package ru.student.volunteer_events.dto;

import lombok.Builder;
import lombok.Data;
import ru.student.volunteer_events.enums.UserRole;

import java.util.Set;

@Data
@Builder
public class UserProfileDto {
    private Long id;
    private String username;
    private String fullName;
    private String phone;
    private UserRole role;
    private Set<String> skills;
    private Double totalVolunteerHours;
}
