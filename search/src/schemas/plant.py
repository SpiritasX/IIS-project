from datetime import datetime
from pydantic import BaseModel

class PlantBase(BaseModel):
    id: int
    priceId: int | None = None
    name: str
    description: str
    varietyId: int
    varietyName: str
    speciesId: int
    speciesName: str
    plantTypeId: int
    plantTypeName: str
    price: float
    createdAt: datetime

class PlantCreate(PlantBase):
    pass

class Plant(PlantBase):
    pass
