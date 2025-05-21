CREATE EXTENSION IF NOT EXISTS vector;

CREATE TYPE UserRole AS ENUM ('CANDIDATE', 'HR');

CREATE TABLE Users (
    userID UUID PRIMARY KEY,
    fullName VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    passwordHash TEXT NOT NULL,
    role UserRole NOT NULL,
    creationTimestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TYPE JobStatus AS ENUM ('OPEN', 'CLOSED', 'DRAFT');
CREATE TYPE RequirementType AS ENUM ('SKILL', 'EXPERIENCE', 'EDUCATION', 'OTHER');

CREATE TABLE JobPostings (
    jobID UUID PRIMARY KEY,
    title VARCHAR(255),
    description TEXT,
    status JobStatus,
    creationTimestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    closingDate DATE,
    lastModifiedTimestamp TIMESTAMP,
    hrCreatorID UUID REFERENCES Users(userID),
    lastModifiedByUserID UUID REFERENCES Users(userID)
);

CREATE TABLE JobRequirements (
    requirementID UUID PRIMARY KEY,
    jobID UUID REFERENCES JobPostings(jobID) ON DELETE CASCADE,
    description TEXT,
    type RequirementType,
    isMandatory BOOLEAN
);

CREATE TYPE ApplicationStatus AS ENUM ('SUBMITTED', 'IN_REVIEW', 'SHORTLISTED', 'REJECTED', 'INTERVIEWING', 'OFFERED', 'HIRED');

CREATE TABLE Applications (
    applicationID UUID PRIMARY KEY,
    submissionTimestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ApplicationStatus,
    resumeContent TEXT,
    originalResumeFilename VARCHAR(255),
    originalResumeFileReference VARCHAR(255),
    lastModifiedTimestamp TIMESTAMP,
    candidateID UUID REFERENCES Users(userID),
    jobID UUID REFERENCES JobPostings(jobID),
    lastModifiedByUserID UUID REFERENCES Users(userID)
);

CREATE TYPE FilterStatus AS ENUM ('PASSED_FILTER', 'FAILED_FILTER', 'NOT_EVALUATED');

CREATE TABLE Assessments (
    assessmentID UUID PRIMARY KEY,
    assessmentTimestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    overallScore FLOAT,
    summary TEXT,
    resumeAnalysisDetails JSON,
    filterStatus FilterStatus,
    lastModifiedTimestamp TIMESTAMP,
    applicationID UUID REFERENCES Applications(applicationID),
    lastModifiedByUserID UUID REFERENCES Users(userID)
);

CREATE TABLE ChatSessions (
    sessionID UUID PRIMARY KEY,
    startTime TIMESTAMP,
    endTime TIMESTAMP,
    assessmentID UUID REFERENCES Assessments(assessmentID)
);

CREATE TYPE MessageSender AS ENUM ('AI', 'CANDIDATE');

CREATE TABLE ChatMessages (
    messageID UUID PRIMARY KEY,
    sender MessageSender,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    content TEXT,
    messageOrder INTEGER,
    sessionID UUID REFERENCES ChatSessions(sessionID)
);

-- Store embeddings linked to documents or concepts used in GenAI
CREATE TABLE Embeddings (
    embeddingID UUID PRIMARY KEY,
    documentReference TEXT, -- where the embedding comes from (the link to the document)
    content TEXT,
    embedding VECTOR(1536), -- adjust dimension as per model used (this one is text-embedding-ada-002	OpenAI	1536)
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);