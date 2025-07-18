from pathlib import Path

from langchain_core.documents import Document

from app.rag import retrieval

cwd = Path.cwd()
docs_path = cwd / "app" / "docs"


def init_db():
    docs = []
    for file in docs_path.iterdir():
        if file.is_file():
            with open(file, "r", encoding="utf-8") as f:
                content = f.read()
                index = 1
                for paragraph in content.split("##"):
                    docs.append(Document(page_content=paragraph, metadata={"source": file.name}, id=index))
                    index += 1
    retrieval.vector_store.add_documents(docs)
