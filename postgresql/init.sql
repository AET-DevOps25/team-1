-- =======================================
-- 🏗 0. Create dedicated databases
-- =======================================
CREATE DATABASE user_db;
CREATE DATABASE job_db;
CREATE DATABASE application_db;
CREATE DATABASE ai_db;

-- =======================================
-- 👤 1. Users schema (user_db)
-- =======================================
\connect user_db

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE user_role AS ENUM ('CANDIDATE', 'HR');

CREATE TABLE users (
    user_id       UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    full_name     VARCHAR(255) NOT NULL,
    email         VARCHAR(255) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    role          user_role NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);

-- =======================================
-- 📄 2. Jobs schema (job_db)
-- =======================================
\connect job_db

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE job_status AS ENUM ('OPEN', 'CLOSED', 'DRAFT');

CREATE TABLE jobs (
    job_id        UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title         VARCHAR(255) NOT NULL,
    description   TEXT NOT NULL,
    requirements  TEXT NOT NULL,
    status        job_status DEFAULT 'DRAFT',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP,
    hr_creator_id UUID NOT NULL -- logical foreign-key reference
);

CREATE INDEX idx_jobs_status     ON jobs(status);
CREATE INDEX idx_jobs_hr_creator ON jobs(hr_creator_id);

-- =======================================
-- 📝 3. Applications schema (application_db)
-- =======================================
\connect application_db

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE application_status AS ENUM (
    'SUBMITTED', 'AI_SCREENING', 'AI_INTERVIEW',
    'COMPLETED', 'SHORTLISTED', 'REJECTED', 'HIRED'
);
CREATE TYPE chat_status         AS ENUM ('ACTIVE', 'COMPLETE');
CREATE TYPE message_sender      AS ENUM ('AI', 'CANDIDATE');
CREATE TYPE decision_enum       AS ENUM ('SHORTLISTED', 'REJECTED', 'HIRED');
CREATE TYPE recommendation_enum AS ENUM ('RECOMMEND', 'CONSIDER', 'NOT_RECOMMEND');

-- Applications
CREATE TABLE applications (
    application_id  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    job_id          UUID NOT NULL,
    candidate_id    UUID NOT NULL,
    status          application_status DEFAULT 'SUBMITTED',
    resume_text     TEXT NOT NULL,
    resume_file_path TEXT,
    hr_decision     decision_enum,
    hr_comments     TEXT,
    submitted_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP
);

CREATE INDEX idx_app_job  ON applications(job_id);
CREATE INDEX idx_app_user ON applications(candidate_id);

-- Assessments
CREATE TABLE assessments (
    assessment_id     UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    application_id    UUID NOT NULL,
    resume_score      FLOAT CHECK (resume_score BETWEEN 0 AND 100),
    interview_score   FLOAT CHECK (interview_score BETWEEN 0 AND 100),
    resume_comment    TEXT,
    interview_comment TEXT,
    recommendation    recommendation_enum,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP
);

CREATE INDEX idx_ass_app ON assessments(application_id);

-- Chat sessions
CREATE TABLE chat_sessions (
    session_id     UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    application_id UUID NOT NULL UNIQUE,
    status         chat_status DEFAULT 'ACTIVE',
    message_count  INTEGER DEFAULT 0,
    started_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at   TIMESTAMP
);

-- Chat messages
CREATE TABLE chat_messages (
    message_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID NOT NULL,
    sender     message_sender NOT NULL,
    content    TEXT NOT NULL,
    sent_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_chat_sess ON chat_messages(session_id);

-- =======================================
-- 🧠 4. AI schema (ai_db)
-- =======================================
\connect ai_db

CREATE EXTENSION IF NOT EXISTS vector;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE embeddings (
    embedding_id SERIAL PRIMARY KEY,
    document_ref TEXT,
    content      TEXT,
    embedding    VECTOR(1536),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_embeddings_vector_hnsw
ON embeddings
USING hnsw (embedding vector_l2_ops)
WITH (
    m = 16,
    ef_construction = 64
);