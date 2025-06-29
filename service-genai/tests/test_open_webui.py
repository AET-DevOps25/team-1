import unittest
import os
import sys
from pydoc import describe

from app.llm.open_webui import schema_chat

# Add the project root to the Python path
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))


class TestOpenWebUI(unittest.TestCase):

    def test_schema_json_generate_response(self):
        """
        Tests the schema_json_generate_response function.
        Note: This is an integration test and requires a running Ollama instance
        and a valid OLLAMA_API_KEY in the .env file.
        """
        if not os.getenv("OLLAMA_API_KEY"):
            self.skipTest("OLLAMA_API_KEY is not set, skipping integration test.")

        test_schema = {
            "type": "object",
            "properties": {
                "name": {"type": "string"},
                "capital": {"type": "string"},
                "languages": {"type": "array", "items": {"type": "string"}}
            },
            "required": ["name", "capital", "languages"]
        }

        result = schema_chat(
            "Tell me about Canada.",
            test_schema
        )

        self.assertIsNotNone(result)
        self.assertIn("name", result)
        self.assertIn("capital", result)
        self.assertIn("languages", result)
        self.assertIsInstance(result["name"], str)
        self.assertIsInstance(result["capital"], str)
        self.assertIsInstance(result["languages"], list)
        print(f"Successfully received and validated structured response: {result}")


if __name__ == '__main__':
    unittest.main()
