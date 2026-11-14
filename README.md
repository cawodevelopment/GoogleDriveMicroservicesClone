# File Storage Microservices

A Docker Compose project providing JWT-based authentication and authenticated file
storage behind an Nginx reverse proxy.

## Architecture

| Service | Technology | Purpose |
| --- | --- | --- |
| `nginx` | Nginx | Public HTTP gateway on port `80` |
| `authentication` | Spring Boot, Java 17 | User registration, login, and JWT creation |
| `authentication-db` | PostgreSQL 16 | Stores user accounts |
| `file-storage` | FastAPI, Python 3.12 | Uploads, lists, downloads, and deletes files |

Files are stored in the `file_storage_data` Docker volume. PostgreSQL data is
stored in the `authentication_db_data` volume.

## Prerequisites

- Docker with Docker Compose
- OpenSSL, for generating the JWT key pair

## Setup

1. Copy the environment template:

   ```bash
   cp .env.example .env
   ```

2. Set a strong `POSTGRES_PASSWORD` in `.env`. The available variables are:

   ```dotenv
   POSTGRES_DB=authentication
   POSTGRES_USER=authentication
   POSTGRES_PASSWORD=replace-with-a-strong-password
   JWT_EXPIRATION_MS=3600000
   ```

3. Generate the RSA keys used to sign and verify JWTs:

   ```bash
   mkdir -p secrets/keys
   openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 \
     -out secrets/keys/jwt-private.pem
   openssl rsa -pubout \
     -in secrets/keys/jwt-private.pem \
     -out secrets/keys/jwt-public.pem
   ```

   Keep both key files private and do not commit them.

4. Build and start the services:

   ```bash
   docker compose up -d --build
   ```

5. Check service status and logs:

   ```bash
   docker compose ps
   docker compose logs -f authentication
   ```

The API is available at `http://localhost`.

## Authentication API

The gateway exposes the authentication service under `/auth`.

### Register

```bash
curl -X POST http://localhost/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"callum@example.com","password":"example123!"}'
```

Returns a JWT with HTTP `201 Created`.

### Login

```bash
curl -X POST http://localhost/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"callum@example.com","password":"example123!"}'
```

Returns a JWT with HTTP `200 OK`.

Passwords must be at least eight characters long, and email addresses must be
valid. Duplicate registrations return `409 Conflict`.

## File API

Include the JWT returned by registration or login as a bearer token.

### Upload a file

```bash
curl -X POST http://localhost/files \
  -H "Authorization: Bearer $TOKEN" \
  -F 'file=@./example.txt'
```

### List files

```bash
curl http://localhost/files \
  -H "Authorization: Bearer $TOKEN"
```

### Download a file

```bash
curl http://localhost/files/example.txt \
  -H "Authorization: Bearer $TOKEN" \
  -o example.txt
```

### Delete a file

```bash
curl -X DELETE http://localhost/files/example.txt \
  -H "Authorization: Bearer $TOKEN"
```

All file endpoints require a valid, non-expired JWT. The file service accepts
only the `RS256` algorithm and verifies tokens with
`secrets/keys/jwt-public.pem`.

## Development

The authentication service can be tested locally with Maven:

```bash
cd authentication
bash mvnw test
```

Build only the authentication service:

```bash
docker compose build authentication
```

Stop the stack without removing persistent volumes:

```bash
docker compose down
```

To remove the containers and stored database/files, use this destructive
command:

```bash
docker compose down -v
```

## Project layout

```text
.
├── authentication/       Spring Boot authentication service
├── file-storage/         FastAPI file service
├── nginx.conf            Reverse-proxy configuration
├── compose.yaml          Docker Compose services and volumes
├── secrets/keys/         Local JWT key pair (ignored by Git)
└── .env.example          Environment variable template
```
