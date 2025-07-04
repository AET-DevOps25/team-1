import unittest
import os
import sys
from app.llm.open_webui import schema_chat

# Add the project root to the Python path
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))


class TestOpenWebUI(unittest.TestCase):

    def test_schema_chat_score_resume(self):
        """
        Tests the schema_chat function with the score_resume tool.
        Note: This is an integration test and requires a running Ollama instance
        and a valid OLLAMA_API_KEY in the .env file.
        """
        if not os.getenv("OLLAMA_API_KEY"):
            self.skipTest("OLLAMA_API_KEY is not set, skipping integration test.")

        tool = {
            "type": "function",
            "function": {
                "name": "score_resume",
                "description": "Scores a resume against job title, job requirements, and resume text.",
                "parameters": {
                    "type": "object",
                    "properties": {
                        "score": {
                            "type": "number",
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
        prompt_system = (
            "You are a strict and experienced AI HR expert. "
            "Your task is to evaluate resumes for a given job. "
            "Always use the 'score_resume' function to return your evaluation. "
            "Do not answer directly; only call the function."
        )
        job_title = "DevOps Engineer"
        job_description = "Develop backend systems and operate on k8s."
        job_requirements = "5 years experience in backend development. Proficient in Kubernetes."
        resume_text = "I have tech stack of React, Vue."
        message_user = (f"Please evaluate the following resume.\n"
                        f"Job title: {job_title}\n"
                        f"Job description: {job_description}\n"
                        f"Job requirements: {job_requirements}\n"
                        f"Resume: {resume_text}")

        response = schema_chat(
            prompt_system=prompt_system,
            message_user=message_user,
            tool=tool
        )

        print(f"Received response from schema_chat: {response}")

        self.assertIsNotNone(response)

        arguments = response

        self.assertIn("score", arguments)
        self.assertIn("score_reason", arguments)
        self.assertIn("recommendation", arguments)
        self.assertIsInstance(arguments["score"], (int, float))
        self.assertIsInstance(arguments["score_reason"], str)
        self.assertIn(arguments["recommendation"], ["RECOMMEND", "CONSIDER", "NOT_RECOMMEND"])
        print(f"Successfully received and validated tool call: {arguments}")


if __name__ == '__main__':
    unittest.main()
