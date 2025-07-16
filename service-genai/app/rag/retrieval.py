import os

from dotenv import load_dotenv
from langchain.chains import RetrievalQA
from langchain_core.output_parsers import StrOutputParser
from langchain_core.prompts import ChatPromptTemplate
from langchain_postgres.vectorstores import PGVector

from app.llm.embedding import OllamaEmbeddings
from app.llm.open_webui import get_chat_llm

load_dotenv()

username = os.getenv("DB_USER")
password = os.getenv("DB_PASSWORD")
host = os.getenv("DB_HOST")
port = os.getenv("DB_PORT")

CONNECTION_STRING = f"postgresql+psycopg://{username}:{password}@{host}:{port}/ai_db"

ollama_embeddings = OllamaEmbeddings()

vector_store = PGVector(
    connection=CONNECTION_STRING,
    embeddings=ollama_embeddings,
    embedding_length=ollama_embeddings.dim,
    collection_name="company_docs",
    # TODO: comment below when prod
    # pre_delete_collection=True
)


def query_rag(query):
    query = f"Requirements: If the question is not related to the context, return 'I don't know, please contact HR for more information'.\n{query}\nAnswer:"
    qa_chain = RetrievalQA.from_chain_type(
        llm=get_chat_llm(),
        retriever=vector_store.as_retriever(search_kwargs={"k": 2}),
        # return_source_documents=True
    )
    result = qa_chain.invoke({"query": query}, return_only_outputs=True)
    return result


def query_rag_stream(query):
    llm = get_chat_llm()
    docs = vector_store.as_retriever(search_kwargs={"k": 2}).invoke(query)
    context = "\n\n".join([doc.page_content for doc in docs])
    prompt = ChatPromptTemplate.from_template(
        "Requirements: If the question is not related to the context, return 'I don't know, please contact HR for more information'.\nQuestion: {query}\nContext: {context}\nAnswer:"
    )
    parser = StrOutputParser()
    chain = prompt | llm | parser
    for chunk in chain.stream({"query": query, "context": context}):
        yield chunk
