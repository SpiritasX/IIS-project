from fastapi import APIRouter
from src.api.deps import get_user_service
from src.schemas.user import UserCreate

router = APIRouter(prefix="/users")

@router.post("/")
def create_user(user_in: UserCreate):
    get_user_service().create_user(user_in)
    return user_in

@router.get("/{user_id}")
def get_user(user_id: int):
    return get_user_service().get_user(user_id)

@router.get("/")
def get_users(skip: int = 0, limit: int = 10):
    return get_user_service().get_users(skip, limit)

@router.put("/{user_id}")
def update_user(user_id: int, user_in: dict):
    get_user_service().update_user(user_id, user_in)
    return get_user_service().get_user(user_id)

@router.delete("/{user_id}")
def delete_user(user_id: int):
    get_user_service().delete_user(user_id)
    return None
