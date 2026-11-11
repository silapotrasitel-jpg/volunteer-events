package ru.student.volunteer_events.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.student.volunteer_events.domain.entity.Application;
import ru.student.volunteer_events.domain.entity.Skill;
import ru.student.volunteer_events.domain.entity.User;
import ru.student.volunteer_events.domain.repository.ApplicationRepository;
import ru.student.volunteer_events.domain.repository.SkillRepository;
import ru.student.volunteer_events.domain.repository.UserRepository;
import ru.student.volunteer_events.dto.SkillRequest;
import ru.student.volunteer_events.dto.UserProfileDto;
import ru.student.volunteer_events.enums.ApplicationStatus;
import ru.student.volunteer_events.exception.BusinessException;
import ru.student.volunteer_events.exception.NotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillAndUserService {

    private final SkillRepository skillRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;

    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }

    @Transactional
    public Skill createSkill(SkillRequest request) {
        if (skillRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new BusinessException("Такой навык уже существует");
        }
        Skill skill = Skill.builder()
                .name(request.getName().trim())
                .build();
        return skillRepository.save(skill);
    }

    @Transactional(readOnly = true)
    public UserProfileDto getProfile(String username) {
        User user = getUserByUsername(username);
        double totalHours = applicationRepository.findByVolunteerId(user.getId()).stream()
                .filter(app -> app.getStatus() == ApplicationStatus.ATTENDED)
                .mapToDouble(app -> app.getCreditedHours() != null ? app.getCreditedHours() : 0.0)
                .sum();

        return UserProfileDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .skills(user.getSkills().stream().map(Skill::getName).collect(Collectors.toSet()))
                .totalVolunteerHours(totalHours)
                .build();
    }

    @Transactional
    public UserProfileDto updateMySkills(String username, Set<Long> skillIds) {
        User user = getUserByUsername(username);
        List<Skill> foundSkills = skillRepository.findAllById(skillIds);
        if (foundSkills.size() != skillIds.size()) {
            throw new NotFoundException("Один или несколько навыков не найдены");
        }
        user.setSkills(new HashSet<>(foundSkills));
        userRepository.save(user);
        return getProfile(username);
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + username));
    }
}
