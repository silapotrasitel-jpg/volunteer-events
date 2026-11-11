--liquibase formatted sql

--changeset student:4
CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    location VARCHAR(200) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    max_participants INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    coordinator_id BIGINT NOT NULL REFERENCES users(id)
);
