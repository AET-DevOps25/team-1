import logging
import os
import sys
from concurrent import futures

import grpc
import pythonjsonlogger
from langchain_core.messages import SystemMessage, HumanMessage, AIMessage
from prometheus_client import start_http_server, Counter

from app.llm import score_resume, score_interview
from app.llm.open_webui import stream_chat
from app.proto import ai_pb2, ai_pb2_grpc
from app.rag import retrieval

logger = logging.getLogger(__name__)

logHandler = logging.StreamHandler()
formatter = pythonjsonlogger.json.JsonFormatter()
logHandler.setFormatter(formatter)
logging.getLogger().addHandler(logHandler)  # 配置 root logger，所有logger继承
logging.getLogger().setLevel(logging.DEBUG)


def handle_exception(exc_type, exc_value, exc_traceback):
    if issubclass(exc_type, KeyboardInterrupt):
        sys.__excepthook__(exc_type, exc_value, exc_traceback)
        return
    logger.error("Uncaught exception", exc_info=(exc_type, exc_value, exc_traceback))

sys.excepthook = handle_exception

# Ensure the parent directory is in PYTHONPATH so that `proto` is importable when launching
BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), os.pardir))
if BASE_DIR not in sys.path:
    sys.path.append(BASE_DIR)


# Prometheus metrics
REQUEST_COUNTER = Counter("ai_requests_total", "Total number of requests received by method", ["method"], )


class AIService(ai_pb2_grpc.AIServiceServicer):
    """gRPC service implementation with dummy responses."""

    def ChatReply(self, request: ai_pb2.ChatReplyRequest, context):  # type: ignore
        REQUEST_COUNTER.labels("ChatReply").inc()
        logger.info("ChatReply, request:", request)
        # Stream response from LLM

        messages = [
            SystemMessage(content=f"You are a experienced expert in {request.job_title}. You are generate a series of questions for a {request.job_title} position. "
                                  f"When you generate a question, you can also consider the candidate's resume, because it can help to check the authenticity of the resume. "
                                  f"The job description is: {request.job_description} and the job requirements are: {request.job_requirements}. "
                                  f"The resume is: {request.resume_text}. "
                                  f"You only need to ask the candidate a series of questions and refuse the candidate's question regardless of the content. "),
        ]
        if len(request.chat_history) == 0:
            logger.info("empty chat history, generate first question")
            messages.append(HumanMessage(content="Please generate first question starting with hello message."))
        else:
            for msg in request.chat_history:
                if msg.sender == ai_pb2.ChatMessage.Sender.SENDER_CANDIDATE:
                    messages.append(HumanMessage(content=msg.content))
                else:
                    messages.append(AIMessage(content=msg.content))
        for chunk_content in stream_chat(messages):
            yield ai_pb2.ChatReplyResponse(ai_message=chunk_content)

    def NormalQA(self, request: ai_pb2.NormalQARequest, context):  # type: ignore
        REQUEST_COUNTER.labels("QA").inc()
        logger.info("NormalQA, request:", request)
        question = request.question
        is_open_rag = request.is_open_rag
        for chunk_content in retrieval.query_rag_stream(question, is_open_rag):
            yield ai_pb2.ChatReplyResponse(ai_message=chunk_content)

    def ScoreResume(self, request: ai_pb2.ScoreResumeRequest, context):  # type: ignore
        REQUEST_COUNTER.labels("ScoreResume").inc()
        logger.info("ScoreResume, request:", request)
        resume_score, comment, recommendation = score_resume(request.job_title, request.job_description, request.job_requirements, request.resume_text)
        ai_pb2_recommendation = ai_pb2.RecommendationEnum.Value(recommendation)
        return ai_pb2.ScoreResumeResponse(resume_score=resume_score, comment=comment, recommendation=ai_pb2_recommendation,)

    def ScoreInterview(self, request: ai_pb2.ScoreInterviewRequest, context):  # type: ignore
        REQUEST_COUNTER.labels("ScoreInterview").inc()
        logger.info("ScoreInterview, request:", request)
        interview_score, comment, recommendation = score_interview(request.job_title, request.job_description, request.job_requirements, request.chat_history)
        return ai_pb2.ScoreInterviewResponse(interview_score=interview_score, comment=comment, recommendation=ai_pb2.RecommendationEnum.Value(recommendation),)


def serve() -> None:
    """Start Prometheus metrics endpoint and gRPC server."""
    # Expose Prometheus metrics
    start_http_server(8000)
    logger.info("Prometheus metrics available at http://localhost:8000/")

    # Start gRPC server
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    ai_pb2_grpc.add_AIServiceServicer_to_server(AIService(), server)
    server.add_insecure_port("[::]:8079")
    server.start()
    logger.info("gRPC server started on port 8079")

    try:
        server.wait_for_termination()
    except KeyboardInterrupt:
        logger.error("Shutting down gRPC server...")
        server.stop(0)


if __name__ == "__main__":
    serve()
