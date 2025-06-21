import os
import sys
import time
from concurrent import futures

import grpc
from prometheus_client import start_http_server, Counter

from app.proto import ai_pb2, ai_pb2_grpc

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
        # Stream a few dummy tokens back to the client
        dummy_tokens = ["Hello", "this", "is", "a", "dummy", "response.", ]
        for token in dummy_tokens:
            yield ai_pb2.ChatReplyResponse(ai_message=token)
            time.sleep(0.05)  # Simulate streaming latency

    def ScoreResume(self, request: ai_pb2.ScoreResumeRequest, context):  # type: ignore
        REQUEST_COUNTER.labels("ScoreResume").inc()
        return ai_pb2.ScoreResumeResponse(resume_score=0.85, comment="This is a dummy resume score.",
                                          recommendation=ai_pb2.RecommendationEnum.RECOMMEND, )

    def ScoreInterview(self, request: ai_pb2.ScoreInterviewRequest, context):  # type: ignore
        REQUEST_COUNTER.labels("ScoreInterview").inc()
        return ai_pb2.ScoreInterviewResponse(interview_score=0.78, comment="This is a dummy interview score.",
                                             recommendation=ai_pb2.RecommendationEnum.CONSIDER, )


def serve() -> None:
    """Start Prometheus metrics endpoint and gRPC server."""
    # Expose Prometheus metrics
    start_http_server(8000)
    print("Prometheus metrics available at http://localhost:8000/")

    # Start gRPC server
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    ai_pb2_grpc.add_AIServiceServicer_to_server(AIService(), server)
    server.add_insecure_port("[::]:8079")
    server.start()
    print("gRPC server started on port 8079")

    try:
        server.wait_for_termination()
    except KeyboardInterrupt:
        print("Shutting down gRPC server...")
        server.stop(0)


if __name__ == "__main__":
    serve()
