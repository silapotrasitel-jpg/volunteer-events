--liquibase formatted sql

--changeset student:6
CREATE TABLE applications (
    id BIGSERIAL PRIMARY KEY,
    volunteer_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_id BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL,
    applied_at TIMESTAMP NOT NULL,
    coordinator_comment VARCHAR(500),
    credited_hours DOUBLE PRECISION DEFAULT 0.0
);
