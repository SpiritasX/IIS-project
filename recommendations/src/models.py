from pydantic import BaseModel
from enum import Enum

class Plant(BaseModel):
    id: int
    name: str

class PlantVariety(BaseModel):
    id: int
    name: str

class CustomerCreate(BaseModel):
    id: int
    first_name: str
    last_name: str


class PlantCreate(BaseModel):
    id: int
    name: str

class PlantVarietyCreate(BaseModel):
    id: int
    name: str

class SearchCreate(BaseModel):
    customer_id: int
    query: str
    min_price: int | None = None
    max_price: int | None = None
    variety: str | None = None
    species: str | None = None
    type: str | None = None


class PurchaseCreate(BaseModel):
    customer_id: int
    plant_id: int
    quantity: int = 1


class RecommendationUpdate(BaseModel):
    viewed: bool | None = None
    purchased: bool | None = None
