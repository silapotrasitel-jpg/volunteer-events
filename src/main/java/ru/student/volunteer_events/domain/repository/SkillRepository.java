package ru.student.volunteer_events.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.student.volunteer_events.domain.entity.Skill;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    boolean existsByNameIgnoreCase(String name);
}
