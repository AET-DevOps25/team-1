import json
import os

import requests
from langchain_community.chat_models import ChatOllama
from dotenv import load_dotenv
from langchain_core.messages.tool import tool_call
from langchain import tools

load_dotenv()
base_url = os.getenv("OLLAMA_BASE_URL")

if base_url is None:
    raise Exception("OLLAMA_BASE_URL is not set")

if os.getenv("OLLAMA_MODEL") is None:
    raise Exception("OLLAMA_MODEL is not set")

API_KEY = os.getenv("OLLAMA_API_KEY")
if API_KEY is None:
    raise Exception("OLLAMA_API_KEY is not set")

API_URL = f"{base_url}/api/generate"


def get_chat_llm():
    return ChatOllama(
        base_url=os.getenv("OLLAMA_BASE_URL"),
        model=os.getenv("OLLAMA_MODEL"),
        headers={"Authorization": f"Bearer {API_KEY}"},
    )


def stream_chat(prompt: str):
    llm = get_chat_llm()
    llm.stream = True
    for chunk in llm.stream(prompt):
        yield chunk.content


def schema_chat(prompt: str, schema: dict):
    payload = {
        "model": "deepseek-r1:70b",
        "prompt": prompt,
        "stream": False,
        "format": schema
    }
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {API_KEY}",
    }
    resp = requests.post(API_URL, headers=headers, json=payload)
    resp.raise_for_status()
    data = resp.json()
    try:
        structured = json.loads(data["response"])
        print("Decode LLM response Success:", structured)
    except Exception as e:
        print("Decode LLM response Failed:", e)
        print("Original response String:", data["response"])
        structured = None
    return structured
