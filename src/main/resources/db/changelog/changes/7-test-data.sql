--liquibase formatted sql

--changeset student:7
-- Пароль для всех тестовых аккаунтов: 123456
INSERT INTO users (username, password, full_name, phone, role) VALUES
('coord1', '$2a$10$FqRRCWaAFuJnF9HbOs2UY.rmIjiODNmA.FXPZTkaYv1hSX/hQnqFO', 'Иван Петров (Координатор)', '+79991112233', 'COORDINATOR'),
('vol1', '$2a$10$FqRRCWaAFuJnF9HbOs2UY.rmIjiODNmA.FXPZTkaYv1hSX/hQnqFO', 'Анна Смирнова (Волонтёр)', '+79994445566', 'VOLUNTEER'),
('vol2', '$2a$10$FqRRCWaAFuJnF9HbOs2UY.rmIjiODNmA.FXPZTkaYv1hSX/hQnqFO', 'Дмитрий Козлов (Волонтёр)', '+79997778899', 'VOLUNTEER');

INSERT INTO skills (name) VALUES
('Первая помощь'),
('Работа с детьми'),
('Организация мероприятий'),
('Водительские права B');

INSERT INTO user_skills (user_id, skill_id) VALUES
(2, 1),
(2, 2),
(3, 3);

INSERT INTO events (title, description, location, start_time, end_time, max_participants, status, coordinator_id) VALUES
('Городской субботник в парке', 'Уборка территории центрального парка и посадка деревьев', 'Центральный парк, главный вход', '2026-12-01 10:00:00', '2026-12-01 14:00:00', 10, 'OPEN', 1),
('Благотворительный забег', 'Помощь на пунктах питания и навигация участников', 'Набережная реки', '2026-12-10 09:00:00', '2026-12-10 15:00:00', 5, 'PLANNED', 1);

INSERT INTO event_skills (event_id, skill_id) VALUES
(1, 3),
(2, 1),
(2, 3);

INSERT INTO applications (volunteer_id, event_id, status, applied_at, coordinator_comment, credited_hours) VALUES
(2, 1, 'APPROVED', '2026-11-10 12:00:00', 'Ждём вас на мероприятии!', 0.0);
