# PlayTrack - Media Tracker Desktop Application

PlayTrack is a Java Swing desktop app for tracking films, games, and books with a modern UI and local SQLite storage.

## Features

- Dual database setup (`db/auth.db` and `db/playtrack.db`)
- Modern UI with FlatLaf styling
- Category-based media tracking (Films, Games, Books)
- Summary dashboard and profile management
- OTP-based email verification for auth flows

## Tech Stack

- Java 21
- Spring Boot 3
- Java Swing
- FlatLaf
- SQLite (xerial JDBC)

## Prerequisites

- JDK 21 installed

## Quick Start (Development)

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

## Build for Release

```bash
./mvnw -DskipTests package
```

Output artifact:

- `target/playtrack-app-1.0-SNAPSHOT.jar`

Run packaged app:

```bash
java -jar target/playtrack-app-1.0-SNAPSHOT.jar
```

## SMTP / OTP Configuration

Set these environment variables in production:

- `PLAYTRACK_SMTP_HOST`
- `PLAYTRACK_SMTP_PORT` (default `587`)
- `PLAYTRACK_SMTP_USERNAME`
- `PLAYTRACK_SMTP_PASSWORD`

Optional development-only flags:

- `PLAYTRACK_OTP_DEBUG_LOG=true` (logs OTP for debugging)
- `PLAYTRACK_OTP_SIMULATED_FALLBACK=true` (allows fallback when SMTP is not configured)

Do not commit real SMTP credentials.

## Convenience Scripts

- `run.bat` (Windows)
- `run.sh` (Linux/macOS)

Both scripts build the JAR automatically if missing, then run `java -jar`.
