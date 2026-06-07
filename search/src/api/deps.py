from elasticsearch import Elasticsearch
from src.core.elastic import es_client
from src.repositories.user_repository import UserRepository
from src.repositories.plant_repository import PlantRepository
from src.services.user_service import UserService
from src.services.plant_service import PlantService

def get_es_client():
    yield es_client

def get_user_service() -> UserService:
    repo = UserRepository(es_client)
    return UserService(repo)

def get_plant_service() -> PlantService:
    repo = PlantRepository(es_client)
    return PlantService(repo)
