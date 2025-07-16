import os
import sys
import unittest
import uuid

from langchain_core.documents import Document

# Add project root so that `app` can be imported when tests executed directly
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from app.rag import retrieval  # noqa: E402


class TestRetrieval(unittest.TestCase):
    """Integration-style tests for the RAG retrieval pipeline."""

    def setUp(self):
        # Prepare sample documents and load into vector store (once per test run)
        docs = [
            Document(page_content="This is the first document", metadata={"source": "file1"}, id=str(uuid.uuid4())),
            Document(page_content="This is the second document", metadata={"source": "file2"}, id=str(uuid.uuid4())),
            Document(page_content="This is the third document", metadata={"source": "file3"}, id=str(uuid.uuid4())),
            Document(page_content="DevOps is this job", metadata={"source": "file4"}, id=str(uuid.uuid4())),
        ]
        retrieval.vector_store.add_documents(docs)

    def test_query_rag_returns_expected_keys(self):
        result = retrieval.query_rag("What is the job description?")
        self.assertIsInstance(result, dict)
        self.assertIn("result", result)
        self.assertIn("source_documents", result)
        # Ensure we retrieved up to 3 docs as configured
        self.assertLessEqual(len(result["source_documents"]), 3)


if __name__ == "__main__":
    unittest.main()
