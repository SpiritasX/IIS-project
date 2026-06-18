from fastapi import APIRouter
from src.models import CustomerCreate, PlantCreate, PlantVarietyCreate, SearchCreate, PurchaseCreate, RecommendationUpdate
from src.service.recommendation_service import RecommendationService

router = APIRouter(prefix="/api/recommendations")


@router.post("/customer")
def create_customer(payload: CustomerCreate):
    return RecommendationService.create_customer(payload)


@router.delete("/customer/{customer_id}")
def delete_customer(customer_id: int):
    return RecommendationService.delete_customer(customer_id)


@router.post("/customer/{customer_id}/view/{plant_id}")
def create_customer_view(customer_id: int, plant_id: int):
    return RecommendationService.create_customer_view(customer_id, plant_id)


@router.get("/customer/{customer_id}/like/{plant_id}")
def get_customer_like(customer_id: int, plant_id: int):
    return RecommendationService.get_customer_like(customer_id, plant_id)


@router.post("/customer/{customer_id}/like/{plant_id}")
def create_customer_like(customer_id: int, plant_id: int):
    return RecommendationService.create_customer_like(customer_id, plant_id)


@router.delete("/customer/{customer_id}/like/{plant_id}")
def unlike_customer_like(customer_id: int, plant_id: int):
    return RecommendationService.unlike_customer_like(customer_id, plant_id)


@router.post("/plant")
def create_plant(payload: PlantCreate):
    return RecommendationService.create_plant(payload)


@router.delete("/plant/{plant_id}")
def delete_plant(plant_id: int):
    return RecommendationService.delete_plant(plant_id)


@router.post("/plant/variety")
def create_plant_variety(payload: PlantVarietyCreate):
    return RecommendationService.create_plant_variety(payload)


@router.post("/search")
def create_search(payload: SearchCreate):
    return RecommendationService.create_search(payload)


@router.post("/purchase")
def create_purchase(payload: PurchaseCreate):
    return RecommendationService.create_purchase(payload)


@router.get("/")
def get_general_recommendations():
    return RecommendationService.get_general_recommendations()


@router.patch("/{recommendation_id}")
def update_recommendation(recommendation_id: str, payload: RecommendationUpdate):
    return RecommendationService.update_recommendation(recommendation_id, payload)


@router.get("/customer/{customer_id}")
def get_customer_recommendations(customer_id: int):
    return RecommendationService.get_customer_recommendations(customer_id)
