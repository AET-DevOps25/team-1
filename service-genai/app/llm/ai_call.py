from .open_webui import schema_chat


score_resume_tool = {
    "type": "function",
    "function": {
        "name": "score_resume",
        "description": "Scores a resume against job title, job description, job requirements, and resume text.",
        "parameters": {
            "type": "object",
            "properties": {
                "score": {
                    "type": "string",
                    "description": "The resume score from 0 to 1, use float number"
                },
                "score_reason": {
                    "type": "string",
                    "description": "The reason for the resume score"
                },
                "recommendation": {
                    "type": "string",
                    "description": "The recommendation for the resume score, choose from RECOMMEND, CONSIDER, NOT_RECOMMEND",
                    "enum": ["RECOMMEND", "CONSIDER", "NOT_RECOMMEND"]
                }
            },
            "required": ["score", "score_reason", "recommendation"]
        }
    }
}

score_interview_tool = {
    "type": "function",
    "function": {
        "name": "score_interview",
        "description": "Scores an interview chat against job title, job description, job requirements, and chat history.",
        "parameters": {
            "type": "object",
            "properties": {
                "score": {
                    "type": "string",
                    "description": "The interview score from 0 to 1, use float number"
                },
                "score_reason": {
                    "type": "string",
                    "description": "The reason for the interview score"
                },
                "recommendation": {
                    "type": "string",
                    "description": "The recommendation for the interview score, choose from RECOMMEND, CONSIDER, NOT_RECOMMEND",
                    "enum": ["RECOMMEND", "CONSIDER", "NOT_RECOMMEND"]
                }
            },
            "required": ["score", "score_reason", "recommendation"]
        }
    }
}

score_resume_prompt_system = (
    "You are a strict and experienced AI HR expert. "
    "Your task is to evaluate resumes for a given job. "
    "Always use the 'score_resume' function to return your evaluation. "
    "Do not answer directly; only call the function."
)

score_interview_prompt_system = (
    "You are a strict and experienced AI HR expert. "
    "Your task is to evaluate the candidate answers in the interviews for a given job. "
    "Always use the 'score_interview' function to return your evaluation. "
    "Do not answer directly; only call the function."
)


def score_resume(job_title, job_description, job_requirements, resume_text):
    message_user = (f"Please evaluate the following resume.\n"
                    f"Job title: {job_title}\n"
                    f"Job description: {job_description}\n"
                    f"Job requirements: {job_requirements}\n"
                    f"Resume: {resume_text}")
    response = schema_chat(
        prompt_system=score_resume_prompt_system,
        message_user=message_user,
        tool=score_resume_tool
    )
    return float(response["score"]), response["score_reason"], response["recommendation"]


def score_interview(job_title, job_description, job_requirements, chat_history_list):
    chat_history = "\n".join(f"[{msg.sender}] {msg.content}" for msg in chat_history_list)
    message_user = (f"Please evaluate the following interview.\n"
                    f"Job title: {job_title}\n"
                    f"Job description: {job_description}\n"
                    f"Job requirements: {job_requirements}\n"
                    f"Chat history: {chat_history}")
    response = schema_chat(
        prompt_system=score_interview_prompt_system,
        message_user=message_user,
        tool=score_interview_tool
    )
    return float(response["score"]), response["score_reason"], response["recommendation"]
