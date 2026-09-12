CREATE TABLE IF NOT EXISTS users
(
    id      BIGSERIAL PRIMARY KEY,
    chat_id BIGINT NOT NULL UNIQUE,
    name    TEXT
);

CREATE TABLE IF NOT EXISTS tasks
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT    NOT NULL REFERENCES users (id),
    task_text  TEXT      NOT NULL,
    is_done    BOOLEAN   NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS rps_rounds
(
    id        BIGSERIAL PRIMARY KEY,
    user_id   BIGINT      NOT NULL REFERENCES users (id),
    result    VARCHAR(32) NOT NULL CHECK ( result in ('WIN', 'LOSE', 'DRAW') ),
    played_at timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS guess_games
(
    id        BIGSERIAL PRIMARY KEY,
    user_id   BIGINT      NOT NULL REFERENCES users (id),
    attempts  INT         NOT NULL CHECK ( attempts > 0 ),
    played_at timestamptz NOT NULL DEFAULT now()
);
