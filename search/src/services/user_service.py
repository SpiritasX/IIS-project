from src.repositories.user_repository import UserRepository
from src.schemas.user import UserCreate, User
from fastapi import HTTPException

class UserService:
    def __init__(self, user_repo: UserRepository):
        self.user_repo = user_repo

    def create_user(self, user_in: UserCreate):
        return self.user_repo.create(user_in.id, user_in.model_dump())

    def get_user(self, user_id: int):
        data = self.user_repo.get_by_id(user_id)

        user = User(**data)

        if not user:
            raise HTTPException(status_code=404, detail="User not found")

        return user

    def get_users(self, skip: int = 0, limit: int = 10):
        data_list = self.user_repo.get_all(skip, limit)
        return [User(**data) for data in data_list]

    def update_user(self, user_id: int, user_in):
        return self.user_repo.update(user_id, user_in)

    def delete_user(self, user_id: int):
        return self.user_repo.delete(user_id)

    def search_users(self, **kwargs):
        return self.user_repo.search_users(**kwargs)

    def search_reports(self, **kwargs):
        return self.user_repo.search_reports(**kwargs)

    def sync_reports(self, user_id: int, reports):
        user = self.get_user(user_id)
        if not user:
            return None
        return self.user_repo.update(user_id, {"reports": reports, "totalReports": len(reports)})
