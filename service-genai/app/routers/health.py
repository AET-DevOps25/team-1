from typing import Dict

from fastapi import APIRouter

router = APIRouter(
    prefix="/health",
    tags=["health"],
    responses={404: {"description": "Not found"}},
)


@router.get("/", response_model=Dict[str, str])
async def health_check():
    """
    Health check endpoint to verify the service is running.
    """
    return {"status": "healthy"}
