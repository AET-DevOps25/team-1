import uvicorn
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.routers import health, resume, chat, assessment

# Create FastAPI app
app = FastAPI(
    title="AIHR Service",
    description="AI-powered HR service for resume filtering, interviewing, and assessment",
    version="0.1.0"
)

# Add CORS middleware
app.add_middleware(
    CORSMiddleware,     # type: ignore
    allow_origins=[
        "https://aihr.student.k8s.aet.cit.tum.de",
        "http://localhost:3000",
        "http://localhost:5173",
        "http://localhost:4200",
    ],
    allow_credentials=True,
    allow_methods=["GET", "POST", "PUT", "DELETE", "OPTIONS"],
    allow_headers=["*"],
)

# Include routers
app.include_router(health.router)
app.include_router(resume.router)
app.include_router(chat.router)
app.include_router(assessment.router)

if __name__ == "__main__":
    uvicorn.run("app.main:app", host="0.0.0.0", port=8079, reload=True)
