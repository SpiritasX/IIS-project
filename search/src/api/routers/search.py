from fastapi import APIRouter
from src.api.deps import get_user_service, get_plant_service

router = APIRouter(prefix="/search")

@router.get("/plants")
def search_plants(
    query = None,
    plant_type = None,
    species = None,
    variety = None,
    min_price = None,
    max_price = None,
    sort_by: str = "price",
    order: str = "asc",
    page: int = 1,
    page_size: int = 100
):
    return get_plant_service().search_plants(
        query=query,
        plant_type=plant_type,
        species=species,
        variety=variety,
        min_price=min_price,
        max_price=max_price,
        sort_by=sort_by,
        order=order,
        page=page,
        page_size=page_size
    )

@router.get("/users")
def search_users(
    query = None,
    city = None,
    country = None,
    min_purchases = None,
    min_reports = None,
    sort_by: str = "totalPurchases",
    order: str = "desc",
    page = 1,
    page_size = 10
):
    return get_user_service().search_users(
        query=query,
        city=city,
        country=country,
        min_purchases=min_purchases,
        min_reports=min_reports,
        sort_by=sort_by,
        order=order,
        page=page,
        page_size=page_size
    )

@router.get("/reports")
def search_reports(
    query = None,
    status = None,
    min_rating = None,
    species = None,
    plant_type = None,
    date_from = None,
    date_to = None,
    sort_by: str = "createdAt",
    order: str = "desc"
):
    return get_user_service().search_reports(
        query=query,
        status=status,
        min_rating=min_rating,
        species=species,
        plant_type=plant_type,
        date_from=date_from,
        date_to=date_to,
        sort_by=sort_by,
        order=order
    )
