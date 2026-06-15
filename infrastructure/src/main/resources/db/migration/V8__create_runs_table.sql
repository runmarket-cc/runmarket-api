CREATE TABLE runs
(
    id                  UUID             NOT NULL,
    user_id             UUID             NOT NULL,
    client_run_id       VARCHAR(255)     NOT NULL,
    group_id            VARCHAR(255)     NOT NULL,
    runner_id           VARCHAR(255)     NOT NULL,
    color               VARCHAR(32),
    started_at          TIMESTAMP        NOT NULL,
    ended_at            TIMESTAMP        NOT NULL,
    duration_sec        INTEGER          NOT NULL,
    distance_km         DOUBLE PRECISION NOT NULL,
    avg_pace_sec_per_km INTEGER          NOT NULL,
    created_by          VARCHAR(255)     NOT NULL,
    created_at          TIMESTAMP        NOT NULL,
    updated_by          VARCHAR(255)     NOT NULL,
    updated_at          TIMESTAMP        NOT NULL,
    CONSTRAINT pk_runs PRIMARY KEY (id),
    CONSTRAINT uq_runs_user_client_run UNIQUE (user_id, client_run_id),
    CONSTRAINT fk_runs_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_runs_user_id ON runs (user_id);

CREATE TABLE run_points
(
    id          UUID             NOT NULL,
    run_id      UUID             NOT NULL,
    seq         INTEGER          NOT NULL,
    lat         DOUBLE PRECISION NOT NULL,
    lng         DOUBLE PRECISION NOT NULL,
    accuracy    DOUBLE PRECISION,
    recorded_at TIMESTAMP        NOT NULL,
    CONSTRAINT pk_run_points PRIMARY KEY (id),
    CONSTRAINT fk_run_points_run FOREIGN KEY (run_id) REFERENCES runs (id) ON DELETE CASCADE
);

CREATE INDEX idx_run_points_run_id ON run_points (run_id, seq);
