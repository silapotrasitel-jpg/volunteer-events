--liquibase formatted sql

--changeset student:5
CREATE TABLE event_skills (
    event_id BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    skill_id BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
    PRIMARY KEY (event_id, skill_id)
);
