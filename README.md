# FinanceFlow

Personal finance management API with AI-powered insights, built with Spring Boot 4.1.0 and Java 25.

This project was created to deepen knowledge and practice with Spring Boot, Java, and related technologies, serving as both a learning exercise and a portfolio piece.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Runtime | Java 25, Spring Boot 4.1.0 |
| Database | MySQL (prod), H2 (tests) |
| ORM | JPA / Hibernate |
| Auth | JWT (jjwt 0.12.6), BCrypt, HttpOnly cookies |
| AI | Google Gemini via Spring AI 2.0.0 |
| Docs | SpringDoc OpenAPI 3.0.2 (Swagger UI) |
| Email | Brevo Transactional Email API |
| Build | Maven |

## Architecture

Hexagonal (ports & adapters):

```
domain/        — entities, repository ports, domain services
application/   — use cases, DTOs
infrastructure/— JPA adapters, HTTP controllers, security
```

## Features

### Auth
- Sign up, sign in (JWT + refresh token in HttpOnly cookies)
- Forgot / reset password via email
- Rate limiting per IP (bucket4j-style filter)

### Budgeting
- CRUD transactions (text, image, audio upload)
- Monthly dashboard — totals by category, daily breakdown, prev month comparison
- AI monthly insights via Gemini (cached for closed months)
- CSV export in Brazilian locale (`;` separator, comma decimals)

## Endpoints

Base URL (deploy): `https://financeflow-ifoj.onrender.com`

All paths below are appended to the base URL. Example: `POST https://financeflow-ifoj.onrender.com/auth/public/signup`

### Auth (`/auth/public`)
| Method | Path | Description |
|--------|------|-------------|
| POST | `/signup` | Register |
| POST | `/signin` | Login |
| POST | `/refresh-token` | Refresh JWT |
| POST | `/forgot-password` | Request reset |
| POST | `/reset-password` | Execute reset |

### Transactions (`/transactions`)
| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | List transactions |
| POST | `/text` | Create by text |
| POST | `/image` | Create by image |
| POST | `/audio` | Create by audio |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| GET | `/dashboard/monthly` | Monthly dashboard |
| POST | `/insights` | AI insights |
| GET | `/export/monthly` | Download CSV |

## Environment Variables

| Variable | Description |
|----------|-------------|
| `SPRING_DATASOURCE_URL` | MySQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | DB user |
| `SPRING_DATASOURCE_PASSWORD` | DB password |
| `JWT_SECRET` | 256+ bit secret for JWT signing |
| `BREVO_API_KEY` | Brevo API key (`xkeysib-...`) for transactional email |
| `BREVO_SENDER_EMAIL` | Verified sender email in Brevo |
| `BREVO_SENDER_NAME` | Sender display name (default: `financeflow`) |
| `BREVO_URL` | Brevo API URL (default: `https://api.brevo.com/v3/smtp/email`) |

## Running

```bash
# build & test
./mvnw clean test

# run
./mvnw spring-boot:run
```

Swagger UI: `http://localhost:8080/swagger-ui.html` or `https://financeflow-ifoj.onrender.com/swagger-ui.html`

## Tests

187 tests (unit + integration), H2 in-memory database for test profile.

```bash
./mvnw test
```
