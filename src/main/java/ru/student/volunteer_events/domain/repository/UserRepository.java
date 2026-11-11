package ru.student.volunteer_events.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.student.volunteer_events.domain.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
