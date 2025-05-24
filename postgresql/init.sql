CREATE EXTENSION IF NOT EXISTS vector;

CREATE TYPE user_role AS ENUM ('CANDIDATE', 'HR');

CREATE TABLE users (
    user_id UUID PRIMARY KEY,
    full_name VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    role user_role NOT NULL,
    creation_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TYPE job_status AS ENUM ('OPEN', 'CLOSED', 'DRAFT');
CREATE TYPE requirement_type AS ENUM ('SKILL', 'EXPERIENCE', 'EDUCATION', 'OTHER');

CREATE TABLE job_postings (
    job_id UUID PRIMARY KEY,
    title VARCHAR(255),
    description TEXT,
    status job_status,
    creation_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    closing_date DATE,
    last_modified_timestamp TIMESTAMP,
    hr_creator_id UUID REFERENCES users(user_id),
    last_modified_by_user_id UUID REFERENCES users(user_id)
);

CREATE TABLE job_requirements (
    requirement_id UUID PRIMARY KEY,
    job_id UUID REFERENCES job_postings(job_id) ON DELETE CASCADE,
    description TEXT,
    type requirement_type,
    is_mandatory BOOLEAN
);

CREATE TYPE application_status AS ENUM ('SUBMITTED', 'IN_REVIEW', 'SHORTLISTED', 'REJECTED', 'INTERVIEWING', 'OFFERED', 'HIRED');

CREATE TABLE applications (
    application_id UUID PRIMARY KEY,
    submission_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status application_status,
    resume_content TEXT,
    original_resume_filename VARCHAR(255),
    original_resume_file_reference VARCHAR(255),
    last_modified_timestamp TIMESTAMP,
    candidate_id UUID REFERENCES users(user_id),
    job_id UUID REFERENCES job_postings(job_id),
    last_modified_by_user_id UUID REFERENCES users(user_id)
);

CREATE TYPE filter_status AS ENUM ('PASSED_FILTER', 'FAILED_FILTER', 'NOT_EVALUATED');

CREATE TABLE assessments (
    assessment_id UUID PRIMARY KEY,
    assessment_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    overall_score FLOAT,
    summary TEXT,
    resume_analysis_details JSON,
    filter_status filter_status,
    last_modified_timestamp TIMESTAMP,
    application_id UUID REFERENCES applications(application_id),
    last_modified_by_user_id UUID REFERENCES users(user_id)
);

CREATE TABLE chat_sessions (
    session_id UUID PRIMARY KEY,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    assessment_id UUID REFERENCES assessments(assessment_id)
);

CREATE TYPE message_sender AS ENUM ('AI', 'CANDIDATE');

CREATE TABLE chat_messages (
    message_id UUID PRIMARY KEY,
    sender message_sender,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    content TEXT,
    message_order INTEGER,
    session_id UUID REFERENCES chat_sessions(session_id)
);

-- Store embeddings linked to documents or concepts used in GenAI
CREATE TABLE embeddings (
    embedding_id UUID PRIMARY KEY,
    document_reference TEXT,
    content TEXT,
    embedding VECTOR(1536),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);