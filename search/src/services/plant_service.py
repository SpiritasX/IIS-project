from src.repositories.plant_repository import PlantRepository
from src.schemas.plant import PlantCreate, Plant
from fastapi import HTTPException

class PlantService:
    def __init__(self, plant_repo: PlantRepository):
        self.plant_repo = plant_repo

    def create_plant(self, plant_in: PlantCreate):
        return self.plant_repo.create(plant_in.id, plant_in.model_dump())

    def get_plant(self, plant_id: int):
        data = self.plant_repo.get_by_id(plant_id)

        plant = Plant(**data)

        if not plant:
            raise HTTPException(status_code=404, detail="Plant not found")

        return plant

    def get_plants(self, skip: int = 0, limit: int = 10):
        data_list = self.plant_repo.get_all(skip, limit)
        return [Plant(**data) for data in data_list]

    def update_plant(self, plant_id: int, plant_in):
        return self.plant_repo.update(plant_id, plant_in)

    def delete_plant(self, plant_id: int):
        return self.plant_repo.delete(plant_id)

    def search_plants(self, **kwargs):
        return self.plant_repo.search_plants(**kwargs)
