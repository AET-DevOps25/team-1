import uvicorn
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

# Import routers
from app.routers import health, resume, chat, assessment

# Create FastAPI app
app = FastAPI(
    title="AIHR Service",
    description="AI-powered HR service for resume filtering, interviewing, and assessment",
    version="0.1.0"
)

# Add CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # In production, replace with specific origins
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routers
app.include_router(health.router)
app.include_router(resume.router)
app.include_router(chat.router)
app.include_router(assessment.router)

if __name__ == "__main__":
    uvicorn.run("app.main:app", host="0.0.0.0", port=8079, reload=True)
