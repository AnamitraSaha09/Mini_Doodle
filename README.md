# Mini Doodle - Meeting Scheduling Platform

A high-performance simulation of a meeting scheduling platform using Spring Boot and Java
technologies. The service enables users to manage their time slots, schedule meetings,
and view their custom calendar availability.

## Tech stack

| Concern | Choice |
|---------|--------|
| Language / framework | Java 21 (LTS), Spring Boot 3.3 |
| Persistence | PostgreSQL 16, Spring Data JPA / Hibernate |
| Migrations | Flyway |
| Tests | JUnit 5, Testcontainers |

## Run it locally

Everything comes up with one command — app + Postgres:

```bash
docker-compose up --build
```

Then:

- API base: `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health: `http://localhost:8080/actuator/health`
- Metrics: `http://localhost:8080/actuator/prometheus`

## API walkthrough

All timestamps are ISO-8601 UTC (`Instant`). A typical flow:

### 1. Create a user (their calendar is created automatically)

```bash
curl -s -X POST http://localhost:8080/api/users \
  -H 'Content-Type: application/json' \
  -d '{"name":"Adam Smith","email":"adam@example.com"}'
# → 201 {"id":1,"name":"Adam Smith","email":"adam@example.com"}
```

### 2. Create available time slots

```bash
curl -s -X POST http://localhost:8080/api/users/1/slots \
  -H 'Content-Type: application/json' \
  -d '{"startTime":"2026-07-01T09:00:00Z","endTime":"2026-07-01T09:30:00Z"}'
# → 201 {"id":10,"startTime":"...","endTime":"...","status":"FREE"}
```

Overlapping a slot in the same calendar returns `409 Conflict`. Back-to-back
slots (one ends exactly as the next begins) are allowed.

### 3. List slots (paged, optional status filter)

```bash
curl -s "http://localhost:8080/api/users/1/slots?status=FREE&page=0&size=50"
```

### 4. Modify or delete a slot

```bash
# Move it / toggle status (only non-null fields are applied)
curl -s -X PATCH http://localhost:8080/api/users/1/slots/10 \
  -H 'Content-Type: application/json' \
  -d '{"status":"BUSY"}'

# Delete (rejected with 409 if the slot is booked)
curl -s -X DELETE http://localhost:8080/api/users/1/slots/10
```

### 5. Book a slot into a meeting

```bash
curl -s -X POST http://localhost:8080/api/meetings/users/1/slots/10 \
  -H 'Content-Type: application/json' \
  -d '{"title":"1:1 sync","description":"weekly","participants":["adam@example.com","grace@example.com"]}'
# → 201 meeting; the slot flips to BUSY.
# Booking an already-booked slot → 409 Conflict.
```

### 6. Query aggregated availability for a window

```bash
curl -s "http://localhost:8080/api/users/1/slots/availability?from=2026-07-01T08:00:00Z&to=2026-07-01T18:00:00Z"
# → {"userId":1,"from":"...","to":"...","free":[...],"busy":[...]}
```

### 7. Fetch or cancel a meeting

```bash
curl -s http://localhost:8080/api/meetings/5
curl -s -X DELETE http://localhost:8080/api/meetings/5   # slot returns to FREE
```

## API reference

| Method | Path                                          | Description |
|--------|-----------------------------------------------|-------------|
| `POST` | `/api/users`                                  | Create a user (+ calendar) |
| `GET` | `/api/users/{userId}`                         | Get a user |
| `POST` | `/api/users/{userId}/slots`                   | Create a free slot |
| `GET` | `/api/users/{userId}/slots`                   | List slots (`status`, `page`, `size`) |
| `PATCH` | `/api/users/{userId}/slots/{slotId}`          | Update slot time/status |
| `DELETE` | `/api/users/{userId}/slots/{slotId}`          | Delete a slot |
| `GET` | `/api/users/{userId}/slots/availability`      | Free/busy view (`from`, `to`) |
| `POST` | `/api/meetings/users/{userId}/slots/{slotId}` | Book slot → meeting |
| `GET` | `/api/meetings/{meetingId}`                   | Get a meeting |
| `DELETE` | `/api/meetings/{meetingId}`                   | Cancel a meeting |

Errors use a consistent JSON shape:

```json
{ "status": 409, "error": "Conflict", "message": "Slot 10 is already booked", "timestamp": "..." }
```
