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
4. after filtering, we let the AI ask candidates for some basic information about their resumes or the related technology (May combine some tech doc from vector database). For example, ask question about how they understand the technology in the requirements or talk something about the project/experience in their resume.
5. AI score them
6. HR can see the scores list at the console page. 

### 1.4 How will you integrate GenAI meaningfully?

1. AI filter resume with job requirements
2. AI score candidates by resume, chat history

## 2. Architecture UML diagram

![uml](https://github.com/user-attachments/assets/c1efe068-4269-49f5-a415-13159e806d9c)

## 3. Backlog

1. As a HR, I want to add a new job requirement, so that candidates can apply for it.
2. As a HR, I want to close a job requirement, so that candidates can no longer apply for it.
3. As a candidate, I want to upload my resume, so that I can apply for a job.
4. As a HR, I want the AI to filter resumes, so that I can find qualified candidates.
5. As a HR, I want the AI to generate some questions to ask candidates, so that I can better understand their qualifications.
6. As a candidate, I want to answer AI's questions about my resume or projects, so that I can better present my strengths.
7. As a HR, I want the AI to score candidates based on their resumes and chat history, so that I can find the best candidates.
8. As a HR, I want to view a ranked list of candidate scores, so that I can quickly identify the best candidates.

## 4. commit template

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

e.g.
```bash
git commit -m "feat: add a new feature" \
-m "add a new feature to the project" \
-m "closes: #1234" \
```

type:
- feat: A new feature
- fix: A bug fix
- docs: Documentation only changes
- ci: Changes to our CI configuration files and scripts
- test: Adding missing tests or correcting existing tests
- refactor: Refactor code but not change business logic, e.g. change variable names, structures, code style
- perf: Optimizing performance by improving code logic