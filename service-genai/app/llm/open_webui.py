import requests
from langchain_community.chat_models import ChatOllama

from .constant import base_url, model, API_KEY

API_URL = f"{base_url}/api/generate"


def get_chat_llm():
    return ChatOllama(
        base_url=base_url,
        model=model,
        headers={"Authorization": f"Bearer {API_KEY}"},
    )


def stream_chat(messages):
    llm = get_chat_llm()
    for chunk in llm.stream(messages):
        yield chunk.content


def schema_chat(prompt_system: str, message_user: str, tool: dict):
    payload = {
        "model": model,
        "messages": [
            {
                "role": "system",
                "content": prompt_system
            },
            {
                "role": "user",
                "content": message_user
            }
        ],
        "stream": False,
        "tools": [tool]
    }
    print(payload)
    headers = {
        "Authorization": f"Bearer {API_KEY}",
        "Content-Type": "application/json"
    }
    resp = requests.post(url=f"{base_url}/api/chat", headers=headers, json=payload)
    resp.raise_for_status()
    data = resp.json()
    try:
        structured = data['message']['tool_calls'][0]['function']['arguments']
        print("Decode LLM response Success:", structured)
    except Exception as e:
        print("Decode LLM response Failed:", e)
        print("Original response String:", data)
        structured = None
    return structured
