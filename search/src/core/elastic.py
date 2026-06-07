from elasticsearch import Elasticsearch
from src.core.config import settings

def get_elastic_client() -> Elasticsearch:
    return Elasticsearch(settings.ELASTICSEARCH_URL)

es_client = get_elastic_client()
