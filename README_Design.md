# System Design

## 1. Full Requirements

### 1.1 Architecture

| Item                   | Description                                                                                                                                                                                                                      |
|------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Microservices          | **user · job · application · ai** – each service is backed by its own dedicated PostgreSQL instance                                                                                                                              |
| External entrypoint    | Traefik Ingress → `/api/v1/**` (HTTPS)                                                                                                                                                                                           |
| Internal communication | Kubernetes ClusterIP + DNS; services listen on port **808x** or expose `/internal/api/v1/**`. No JWT is required; traffic is restricted via NetworkPolicy and/or mTLS                                                            |
| Security               | JWT (RS256). All services validate tokens with the same public key. **No refresh tokens and no revocation/black-list**; logging out is done on the frontend by simply discarding the token                                       |
| File storage           | Original resume files are stored in file system; database tables only persist the file path                                                                                                                                      |
| AI chat rules          | Each application is allowed **exactly one** `chat_session` whose lifecycle is `ACTIVE` → `COMPLETE`. The service maintains an internal message counter; when `message_count` > 50 the session is automatically marked `COMPLETE` |

### 1.2 Business Flows

1. **Candidate**: register → log in → browse jobs → apply and upload resume → check application progress / assessment →
   start or continue the AI chat
2. **HR**: log in → create / modify / close job postings → view applications and assessments → set `hr_decision` &
   `hr_comments` → view the entire chat history
3. **AI**: provides chat replies, resume scoring, and interview scoring through gRPC methods

---

## 2. PostgreSQL DDL (four isolated instances)

### 2.1 `user` instance

```sql
CREATE
EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE user_role AS ENUM ('CANDIDATE', 'HR');

CREATE TABLE users
(
    user_id       UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    full_name     VARCHAR(255)        NOT NULL,
    email         VARCHAR(255) UNIQUE NOT NULL,
    password_hash TEXT                NOT NULL,
    role          user_role           NOT NULL,
    created_at    TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users (email);
```

### 2.2 `job` instance

```sql
CREATE TYPE job_status AS ENUM ('OPEN', 'CLOSED', 'DRAFT');

CREATE TABLE jobs
(
    job_id        UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title         VARCHAR(255) NOT NULL,
    description   TEXT         NOT NULL,
    requirements  TEXT         NOT NULL,
    status        job_status       DEFAULT 'DRAFT',
    closing_date  DATE,
    created_at    TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP,
    hr_creator_id UUID         NOT NULL -- logical foreign-key reference
);

CREATE INDEX idx_jobs_status ON jobs (status);
CREATE INDEX idx_jobs_hr_creator ON jobs (hr_creator_id);
```

### 2.3 `application` instance

```sql
CREATE TYPE application_status AS ENUM (
  'SUBMITTED', 'AI_SCREENING', 'AI_INTERVIEW',
  'COMPLETED', 'SHORTLISTED', 'REJECTED', 'HIRED'
);
CREATE TYPE chat_status AS ENUM ('ACTIVE', 'COMPLETE');
CREATE TYPE message_sender AS ENUM ('AI', 'CANDIDATE');
CREATE TYPE decision_enum AS ENUM ('SHORTLISTED', 'REJECTED', 'HIRED');
CREATE TYPE recommendation_enum AS ENUM ('RECOMMEND', 'CONSIDER', 'NOT_RECOMMEND');

-- Applications
CREATE TABLE applications
(
    application_id  UUID PRIMARY KEY   DEFAULT uuid_generate_v4(),
    job_id          UUID NOT NULL,
    candidate_id    UUID NOT NULL,
    status          application_status DEFAULT 'SUBMITTED',
    resume_text     TEXT NOT NULL,
    resume_file_url TEXT,
    hr_decision     decision_enum,
    hr_comments     TEXT,
    submitted_at    TIMESTAMP          DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP
);

CREATE INDEX idx_app_job ON applications (job_id);
CREATE INDEX idx_app_user ON applications (candidate_id);

-- Assessments
CREATE TABLE assessments
(
    assessment_id     UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    application_id    UUID NOT NULL,
    resume_score      FLOAT CHECK (resume_score BETWEEN 0 AND 100),
    interview_score   FLOAT CHECK (interview_score BETWEEN 0 AND 100),
    resume_comment    TEXT,
    interview_comment TEXT,
    recommendation    recommendation_enum,
    created_at        TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP
);

CREATE INDEX idx_ass_app ON assessments (application_id);

-- Chat sessions
CREATE TABLE chat_sessions
(
    session_id     UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    application_id UUID NOT NULL UNIQUE,
    status         chat_status      DEFAULT 'ACTIVE',
    message_count  INTEGER          DEFAULT 0,
    started_at     TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    completed_at   TIMESTAMP
);

-- Chat messages
CREATE TABLE chat_messages
(
    message_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID           NOT NULL,
    sender     message_sender NOT NULL,
    content    TEXT           NOT NULL,
    sent_at    TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_chat_sess ON chat_messages (session_id);
```

### 2.4 `ai` instance

```sql
CREATE
EXTENSION IF NOT EXISTS vector;

CREATE TABLE embeddings
(
    embedding_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    document_ref TEXT,
    content      TEXT,
    embedding    VECTOR(1536),
    created_at   TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_embed_hnsw
    ON embeddings USING hnsw (embedding vector_l2_ops)
    WITH (m = 16, ef_construction = 64);
```

---

## 3. API Overview

> External URL prefix: **`/api/v1`**Internal URL prefix: **`/internal/api/v1`** or port **8081**  
> All request/response bodies are wrapped in a unified `ApiResponse` envelope (examples omitted).

### 3.1 `user` service

| Method | Path                   | Auth     | Description                     |
|--------|------------------------|----------|---------------------------------|
| POST   | `/auth/login`          | Public   | Log in and receive a JWT        |
| POST   | `/auth/register`       | Public   | Candidate registration          |
| POST   | `/auth/hr-register`    | JWT (HR) | HR creates another HR account   |
| GET    | `/internal/users/{id}` | Internal | Retrieve user information by ID |

### 3.2 `job` service

| Method | Path                  | Auth     | Description                                        |
|--------|-----------------------|----------|----------------------------------------------------|
| GET    | `/jobs`               | Public   | Browse jobs (pagination & optional status filter)  |
| GET    | `/jobs/{id}`          | Public   | Job details                                        |
| POST   | `/jobs`               | JWT (HR) | Create job                                         |
| PATCH  | `/jobs/{id}`          | JWT (HR) | Update title / description / requirements / status |
| DELETE | `/jobs/{id}`          | JWT (HR) | Delete job                                         |
| POST   | `/jobs/{id}/close`    | JWT (HR) | Close job                                          |
| POST   | `/jobs/{id}/open`     | JWT (HR) | Re-open job                                        |
| GET    | `/internal/jobs/{id}` | Internal | Job info for the application service               |

### 3.3 `application` service

| Method | Path                          | Auth            | Description                                                                  |
|--------|-------------------------------|-----------------|------------------------------------------------------------------------------|
| POST   | `/applications`               | JWT (Candidate) | Apply for a job; `multipart/form-data` upload resume; returns application ID |
| GET    | `/applications`               | JWT             | Candidate: own applications; HR: paginated search by `job_id` / `status`     |
| GET    | `/applications/{id}`          | JWT             | View application details (including assessment and chat state)               |
| PATCH  | `/applications/{id}`          | JWT (HR)        | Update `hr_decision`, `hr_comments`, or `status`                             |
| GET    | `/applications/{id}/messages` | JWT (HR)        | HR fetches all chat messages                                                 |
| POST   | `/applications/{id}/chat`     | JWT (Candidate) | Create or fetch the chat session for this application                        |
| POST   | `/chat/{session_id}/messages` | JWT (Candidate) | Send message → AI reply; returns `ai_message` + `complete_flag`              |
| GET    | `/chat/{session_id}/messages` | JWT (Candidate) | Candidate paginated view of chat history                                     |

### 3.4 `ai` service (gRPC)

| RPC              | Request                                                         | Response                      |
|------------------|-----------------------------------------------------------------|-------------------------------|
| `ChatReply`      | `session_id`, `resume_text`, `job_requirements`, `chat_history` | `ai_message`, `complete_flag` |
| `ScoreResume`    | `resume_text`, `job_requirements`                               | `resume_score`, `comment`     |
| `ScoreInterview` | `resume_text`, `job_requirements`, `chat_history`               | `interview_score`, `comment`  |

### 3.5 Operations Endpoints (all services)

| Method | Path       | Auth     | Description        |
|--------|------------|----------|--------------------|
| GET    | `/health`  | Public   | Liveness probe     |
| GET    | `/metrics` | Internal | Prometheus metrics |

---

The requirements, database schemas, and APIs above cover the entire business domain: registration & login, job
management, application workflow, AI chat & scoring, HR decisions, and candidate self-service progress tracking.  
JWTs have no refresh/revocation mechanism, and services expose only the minimal internal APIs needed to complete this
robust yet minimal feature loop. 