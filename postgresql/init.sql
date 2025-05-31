-- =======================================
-- 🔧 1. Extensions
-- =======================================
CREATE EXTENSION IF NOT EXISTS vector;

-- =======================================
-- 🔧 2. Enum Types
-- =======================================
CREATE TYPE user_role AS ENUM ('CANDIDATE', 'HR');
CREATE TYPE job_status AS ENUM ('OPEN', 'CLOSED', 'DRAFT');
CREATE TYPE application_status AS ENUM (
    'SUBMITTED',
    'AI_SCREENING',
    'AI_INTERVIEW',
    'COMPLETED',
    'SHORTLISTED',
    'REJECTED',
    'HIRED'
);
CREATE TYPE recommendation_enum AS ENUM ('RECOMMEND', 'CONSIDER', 'NOT_RECOMMEND');
CREATE TYPE chat_status AS ENUM ('ACTIVE', 'COMPLETED', 'EXPIRED');
CREATE TYPE message_sender AS ENUM ('AI', 'CANDIDATE');
CREATE TYPE decision_enum AS ENUM ('SHORTLISTED', 'REJECTED', 'HIRED');

-- =======================================
-- 👤 3. Users Table
-- =======================================
CREATE TABLE users (
    user_id UUID PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    role user_role NOT NULL,
    creation_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =======================================
-- 📄 4. Jobs Table
-- =======================================
CREATE TABLE jobs (
    job_id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    requirements TEXT NOT NULL,
    status job_status NOT NULL DEFAULT 'DRAFT',
    creation_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    closing_date DATE,
    last_modified_timestamp TIMESTAMP,
    hr_creator_id UUID REFERENCES users(user_id) NOT NULL
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_jobs_hr_creator_id ON jobs(hr_creator_id);

-- =======================================
-- 📝 5. Applications Table
-- =======================================
CREATE TABLE applications (
    application_id UUID PRIMARY KEY,
    submission_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status application_status NOT NULL DEFAULT 'SUBMITTED',
    resume_content TEXT NOT NULL,
    original_resume_filename VARCHAR(255),
    last_modified_timestamp TIMESTAMP,
    candidate_id UUID REFERENCES users(user_id) NOT NULL,
    job_id UUID REFERENCES jobs(job_id) NOT NULL,
    hr_decision decision_enum,
    hr_comments TEXT
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_applications_job_id ON applications(job_id);
CREATE INDEX IF NOT EXISTS idx_applications_candidate_id ON applications(candidate_id);

-- =======================================
-- 📊 6. Assessments Table
-- =======================================
CREATE TABLE assessments (
    assessment_id UUID PRIMARY KEY,
    application_id UUID REFERENCES applications(application_id) NOT NULL,
    resume_score FLOAT CHECK (resume_score BETWEEN 0 AND 100),
    interview_score FLOAT CHECK (interview_score BETWEEN 0 AND 100),
    final_score FLOAT CHECK (final_score BETWEEN 0 AND 100),
    resume_analysis TEXT,
    interview_summary TEXT,
    recommendation recommendation_enum,
    creation_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_timestamp TIMESTAMP
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_assessments_application_id ON assessments(application_id);

-- =======================================
-- 💬 7. Chat Sessions Table
-- =======================================
CREATE TABLE chat_sessions (
    session_id UUID PRIMARY KEY,
    application_id UUID REFERENCES applications(application_id) NOT NULL,
    status chat_status DEFAULT 'ACTIVE',
    start_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_timestamp TIMESTAMP,
    message_count INTEGER DEFAULT 0
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_chat_sessions_application_id ON chat_sessions(application_id);

-- =======================================
-- 📨 8. Chat Messages Table
-- =======================================
CREATE TABLE chat_messages (
    message_id UUID PRIMARY KEY,
    session_id UUID REFERENCES chat_sessions(session_id) NOT NULL,
    sender message_sender NOT NULL,
    content TEXT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_chat_messages_session_id ON chat_messages(session_id);

-- =======================================
-- 🧠 9. Embeddings Table (for semantic search)
-- =======================================
CREATE TABLE embeddings (
    embedding_id UUID PRIMARY KEY,
    document_reference TEXT,
    content TEXT,
    embedding VECTOR(1536),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Vector ANN index for semantic similarity search
CREATE INDEX IF NOT EXISTS idx_embeddings_vector_hnsw
ON embeddings
USING hnsw (embedding vector_l2_ops)
WITH (
    m = 16,
    ef_construction = 64
);

-- =======================================
-- 🔁 10. Trigger to update message_count in chat_sessions
-- =======================================
CREATE OR REPLACE FUNCTION increment_message_count()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE chat_sessions
    SET message_count = message_count + 1
    WHERE session_id = NEW.session_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_increment_message_count ON chat_messages;

CREATE TRIGGER trg_increment_message_count
AFTER INSERT ON chat_messages
FOR EACH ROW
EXECUTE FUNCTION increment_message_count();
