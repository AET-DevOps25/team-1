# README

## 1. Introduction

### 1.1 Who are the intended users?
1. Our customers are the HR.
2. Our users are the candidates who apply jobs.

### 1.2 What is the main functionality?

1. Filter resume with qualified by the job requirements or other requirements.
2. Have a simple talk with the candidates and score them.
3. Show the score board to HR.

### 1.3 Describe some scenarios how your app will function?

1. we pretend a IT company career website
2. Candidates upload the resumes to website
3. backend use LLM to filter it with the requirement of the target job
4. after filtering, we let the AI HR to ask candidates with some basic information about their resumes or the related technology (May combine some tech doc from vector database). For example, ask question about how they understand the technology in the requirements or talk something about the project/experience in their resume.
5. AI score them
6. HR can see the scores list at the console page. 

### 1.4 How will you integrate GenAI meaningfully?

1. AI filter resume with job requirements
2. AI score candidates by resume, chat history

## 2. Microservices

1. User authorization service
2. Resume filtering and scoring service
3. AI-powered HR chatbot service
