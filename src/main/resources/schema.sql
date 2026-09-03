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