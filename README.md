# Task Manager (Microservices)
## Stack
- Python/Java/Scala
- Postgres
- API Gateway
- File server
- Docker & Docker-Compose

## Requirements
- Installed Docker
- Installed depend libs

## CLI Tools
- `make help` to see all available instructions
- `make run` to build & run project
- `make db-init` to init db for services

## Build & Run Project
- Run `make run` in terminal window
- **If you are running project locally for the first time,** open new window and run `make db-init && make run `

## TODO like ⭐⭐⭐
- Logging & Tracing

## Endpoints
### POST /user/sign-up

Create New User

**Example Input**:
```json
{
  "first_name": "Kirk",
  "last_name": "Brian",
  "username": "brian-k",
  "password": "mypassword1"
}
```
**Example Response**:
```json
{
  "id": 1
}
```

### POST /user/token

Generate JWT Token

**Example Input**:
```json
{
  "username": "brian-k",
  "password": "mypassword1"
}
```
**Example Response**:
```json
{
  "access_token": "8vZ2F0ZXdheTo4MDgwI<...>"
}
```

### GET /user/profile

Return User Info

**HTTP Headers**:

```
Authorization: Bearer <access_token>
```

**Example Response**:
```json
{
  "id": 1,
  "first_name": "Kirk",
  "last_name": "Brian",
  "username": "brian-k",
  "password": "mypassword1"
}
```

### POST /tasks

Create New Task

**HTTP Headers**:

```
Authorization: Bearer <access_token>
```

**Example Input**:
```json
{
  "title": "Task 1"
}
```
**Example Response**:
```json
{
  "id": 1
}
```

### GET /tasks

Get All Tasks

**HTTP Headers**:

```
Authorization: Bearer <access_token>
```

**Example Response**:
```json
[
    {
      "id": 1,
      "title": "Task 1",
      "created_at": "2022-10-03T016:19:46.674946Z",
      "user_id": 1
    }
]
```

### GET /tasks/1

Get Task By Id

**HTTP Headers**:

```
Authorization: Bearer <access_token>
```

**Example Response**:
```json
{
  "id": 1,
  "title": "Task 1",
  "created_at": "2022-10-03T016:19:46.674946Z",
  "user_id": 1
}
```
