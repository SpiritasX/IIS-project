from fastapi import FastAPI
from src.routes.recommendation_api import router as recommendation_api_router

app = FastAPI()

app.include_router(recommendation_api_router)
