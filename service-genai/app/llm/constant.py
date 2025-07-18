import os

from dotenv import load_dotenv

load_dotenv()
base_url = os.getenv("OLLAMA_BASE_URL")

if base_url is None:
    raise Exception("OLLAMA_BASE_URL is not set")

model = os.getenv("OLLAMA_MODEL")
if model is None:
    raise Exception("OLLAMA_MODEL is not set")

API_KEY = os.getenv("OLLAMA_API_KEY")
if API_KEY is None:
    raise Exception("OLLAMA_API_KEY is not set")