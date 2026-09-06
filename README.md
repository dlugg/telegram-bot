# Telegram To-Do Bot

A Telegram bot for personal task management, reminders and small utilities, written in Java without a framework.

Tasks are stored in PostgreSQL and survive restarts. The project started as an in-memory prototype and was migrated to a database, which is the reason it has an explicit data access layer.

## Features

**Tasks**

- `/add <text>` — add a task
- `/list` — show numbered tasks, completed ones marked with a check
- `/done <number>` — mark a task as completed (it is kept, not deleted)
- `/find <word>` — search tasks by substring, keeping the original numbering
- `/clear` — delete all tasks of the current user

**Reminders**

- `/remind <minutes> <text>` — send a message back after the given delay, with input validation and an upper limit

**Utilities and games**

- `/weather <city>` — current weather via OpenWeather API
- `/btc` — current Bitcoin price
- `/quote` — random anime quote
- `/rps`, `/guess`, `/ball`, `/reverse` — small games, `/stats` for rock-paper-scissors results
- `/who` — asks for your name and remembers it
- `/help` — command list, generated from the command registry rather than hardcoded

## Tech stack

- Java 21
- Maven
- PostgreSQL, accessed through plain JDBC
- JUnit 5
- TelegramBots 10.0.0
- OkHttp and org.json for external APIs

## Architecture

Three layers, each depending only on the one below it.

**Commands** implement a single `Command` interface and are registered in a map, so adding a new command does not require touching the dispatcher. Two-step dialogs (weather, name, games) are driven by a `State` enum instead of string flags.

**Services** hold the business logic and translate `SQLException` into an unchecked `DataAccessException`, so commands never see JDBC types.

**Repositories** own SQL. Every query uses `PreparedStatement` with bound parameters; no string concatenation is used to build SQL. Creating a user together with their first task runs inside a single transaction — either both rows appear or neither does.

Tables:

```sql
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
```

Task numbers shown to the user are positions in an ordered list, not database identifiers — the user never sees internal ids.

## Tests

Unit tests cover formatting logic; integration tests run against a real PostgreSQL database and cover the repository layer: task creation and ordering, position-based removal and completion, boundary cases such as position zero and negative values, isolation between users, and user lookup that must not create rows as a side effect.

Each test starts from a clean state — the tables are truncated before every test method.

Set up the test database once:

```bash
createdb -U postgres javabot_test
psql -U postgres -d javabot_test -f src/main/resources/schema.sql
```

Then run:

```bash
mvn test
```

Tests also run as part of `mvn package`, so a failing test stops the build and no jar is produced.

## Running locally

Requirements: JDK 21, Maven, PostgreSQL.

Create the database and the tables:

```bash
createdb -U postgres javabot
psql -U postgres -d javabot -f src/main/resources/schema.sql
```

Copy `.env.example` to `.env` and fill in your values:

```bash
cp .env.example .env
```

```
BOT_TOKEN=your_telegram_token
WEATHER_API_KEY=your_openweather_key
DATABASE_PASSWORD=your_postgres_password
```

Build and run:

```bash
mvn clean package
./run.sh
```

The script loads `.env` and starts the jar. No secrets are stored in the repository — `.env` is git-ignored, and the bot fails fast on startup if a variable is missing.

## Roadmap

- Tests for the service and command layers
- Migration from plain JDBC to Spring Data JPA
- REST API on top of the same data
- Docker image with the bot and the database
- Scheduled reminders that survive a restart

## Status

Personal learning project, in active development. Written from scratch as part of learning backend Java.