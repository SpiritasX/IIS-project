from fastapi import FastAPI
from src.api.routers import users, plants, search
from src.core.config import settings
from src.core.elastic import es_client
from src.models.mappings import USER_MAPPING, PLANT_MAPPING
from src.seeders.seeder import seed_data

app = FastAPI()

app.include_router(users.router, prefix="/api/search")
app.include_router(plants.router, prefix="/api/search")
app.include_router(search.router, prefix="/api/search")

@app.on_event("startup")
async def startup_event():
    for index, mapping in [(settings.USERS_INDEX, USER_MAPPING), (settings.PLANTS_INDEX, PLANT_MAPPING)]:
        if not es_client.indices.exists(index=index):
            es_client.indices.create(index=index, body=mapping)

    if settings.SEED_DATA:
        seed_data()
