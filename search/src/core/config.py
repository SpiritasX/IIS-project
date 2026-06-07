from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    ELASTICSEARCH_URL: str = "http://elasticsearch:9200"
    
    USERS_INDEX: str = "users"
    PLANTS_INDEX: str = "plants"
    
    SEED_DATA: bool = True
    MIN_DOCS_FOR_SEEDING: int = 1000

    class Config:
        case_sensitive = True

settings = Settings()
