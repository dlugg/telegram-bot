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
- TelegramBots 10.0.0
- OkHttp and org.json for external APIs

## Architecture

Three layers, each depending only on the one below it.

**Commands** implement a single `Command` interface and are registered in a map, so adding a new command does not require touching the dispatcher. Two-step dialogs (weather, name, games) are driven by a `State` enum instead of string flags.

**Services** hold the business logic and translate `SQLException` into an unchecked `DataAccessException`, so commands never see JDBC types.

**Repositories** own SQL. Every query uses `PreparedStatement` with bound parameters; no string concatenation is used to build SQL.

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

## Running locally

Requirements: JDK 21, Maven, PostgreSQL.

Create the database and the tables:

```bash
createdb -U postgres javabot
psql -U postgres -d javabot -f src/main/resources/schema.sql
```

Set the environment variables:

```bash
export BOT_TOKEN=your_telegram_token
export WEATHER_API_KEY=your_openweather_key
export DATABASE_PASSWORD=your_postgres_password
```

Build and run:

```bash
mvn clean package
java -jar target/tgBot-1.0-SNAPSHOT.jar
```

No secrets are stored in the repository; the bot reads all of them from the environment and fails fast on startup if they are missing.

## Roadmap

- Unit tests with JUnit 5, starting with the repository layer
- Transactions for operations that touch two tables
- Migration from plain JDBC to Spring Data JPA
- Docker image with the bot and the database
- Scheduled reminders that survive a restart

## Status

Personal learning project, in active development. Written from scratch as part of learning backend Java.
