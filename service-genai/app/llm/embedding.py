import requests
from langchain_core.embeddings import Embeddings

from app.llm.constant import base_url, model, API_KEY


class OllamaEmbeddings(Embeddings):
    def __init__(self):
        self.model = model
        self.base_url = base_url
        self.dim = 8192

    def embed_documents(self, texts):
        return [self._embed(text) for text in texts]

    def embed_query(self, text):
        return self._embed(text)

    def _embed(self, text):
        resp = requests.post(
            f"{self.base_url}/api/embed",
            json={"model": self.model, "input": [text]},
            headers={"Authorization": f"Bearer {API_KEY}"},
        )
        resp.raise_for_status()
        return resp.json()["embeddings"][0]
