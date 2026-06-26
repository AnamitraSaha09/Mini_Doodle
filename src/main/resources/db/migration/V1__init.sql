CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE app_user (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(320) NOT NULL UNIQUE
);

CREATE TABLE calendar (
    id      BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES app_user (id) ON DELETE CASCADE
);

CREATE TABLE time_slot (
    id          BIGSERIAL PRIMARY KEY,
    calendar_id BIGINT      NOT NULL REFERENCES calendar (id) ON DELETE CASCADE,
    start_time  TIMESTAMPTZ NOT NULL,
    end_time    TIMESTAMPTZ NOT NULL,
    status      VARCHAR(8)  NOT NULL DEFAULT 'FREE',
    version     BIGINT      NOT NULL DEFAULT 0
    CONSTRAINT chk_slot_status CHECK (status IN ('FREE', 'BUSY')),
    CONSTRAINT chk_slot_range  CHECK (end_time > start_time),
    CONSTRAINT excl_slot_overlap EXCLUDE USING gist (
        calendar_id WITH =,
        tsrange(start_time, end_time, '[)') WITH &&
    )
);

CREATE INDEX idx_slot_calendar_time   ON time_slot (calendar_id, start_time, end_time);
CREATE INDEX idx_slot_calendar_status ON time_slot (calendar_id, status);

CREATE TABLE meeting (
    id          BIGSERIAL PRIMARY KEY,
    slot_id     BIGINT       NOT NULL UNIQUE REFERENCES time_slot (id) ON DELETE CASCADE,
    title       VARCHAR(255) NOT NULL,
    description TEXT
);

CREATE TABLE meeting_participant (
    meeting_id BIGINT       NOT NULL REFERENCES meeting (id) ON DELETE CASCADE,
    email      VARCHAR(320) NOT NULL,
    PRIMARY KEY (meeting_id, email)
);
