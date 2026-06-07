from fastapi import APIRouter
from src.api.deps import get_plant_service
from src.schemas.plant import PlantCreate

router = APIRouter(prefix="/plants")

@router.post("")
def create_plant(plant_in: PlantCreate):
    get_plant_service().create_plant(plant_in)
    return plant_in

@router.get("/{plant_id}")
def get_plant(plant_id: int):
    return get_plant_service().get_plant(plant_id)

@router.get("")
def get_plants(skip: int = 0, limit: int = 10):
    return get_plant_service().get_plants(skip, limit)

@router.put("/{plant_id}")
def update_plant(plant_id: int, plant_in: dict):
    get_plant_service().update_plant(plant_id, plant_in)
    return get_plant_service().get_plant(plant_id)

@router.delete("/{plant_id}")
def delete_plant(plant_id: int):
    get_plant_service().delete_plant(plant_id)
    return None
