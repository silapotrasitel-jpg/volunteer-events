package ru.student.volunteer_events.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.student.volunteer_events.domain.entity.Skill;
import ru.student.volunteer_events.dto.SkillRequest;
import ru.student.volunteer_events.dto.UserProfileDto;
import ru.student.volunteer_events.service.SkillAndUserService;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserAndSkillController {

    private final SkillAndUserService service;

    @GetMapping("/skills")
    public ResponseEntity<List<Skill>> getAllSkills() {
        return ResponseEntity.ok(service.getAllSkills());
    }

    @PostMapping("/skills")
    @PreAuthorize("hasRole('COORDINATOR')")
    public ResponseEntity<Skill> createSkill(@Valid @RequestBody SkillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createSkill(request));
    }

    @GetMapping("/users/me")
    public ResponseEntity<UserProfileDto> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(service.getProfile(authentication.getName()));
    }

    @PutMapping("/users/me/skills")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ResponseEntity<UserProfileDto> updateMySkills(
            @RequestBody Set<Long> skillIds,
            Authentication authentication
    ) {
        return ResponseEntity.ok(service.updateMySkills(authentication.getName(), skillIds));
    }
}
