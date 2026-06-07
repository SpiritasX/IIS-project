from src.repositories.base import BaseRepository
from src.core.config import settings

class PlantRepository(BaseRepository):
    def __init__(self, es):
        super().__init__(es, settings.PLANTS_INDEX)

    def search_plants(
        self,
        query = None,
        plant_type = None,
        species = None,
        variety = None,
        min_price = None,
        max_price = None,
        availability = None,
        sort_by: str = "price",
        order: str = "asc"
    ):
        must = []
        if query:
            must.append({
                "multi_match": {
                    "query": query,
                    "fields": ["name^3", "description"],
                    "fuzziness": "AUTO",
                    "operator": "and",
                    "minimum_should_match": "75%",
                }
            })
        else:
            must.append({"match_all": {}})

        filters = []
        if plant_type:
            filters.append({"term": {"plantTypeName": plant_type}})
        if species:
            filters.append({"term": {"speciesName": species}})
        if variety:
            filters.append({"term": {"varietyName": variety}})
        
        price_range = {}
        if min_price is not None:
            price_range["gte"] = min_price
        if max_price is not None:
            price_range["lte"] = max_price
        if price_range:
            filters.append({"range": {"price": price_range}})
            
        if availability is not None:
            filters.append({"range": {"availability": {"gte": availability}}})

        search_query = {
            "bool": {
                "must": must,
                "filter": filters
            }
        }

        aggregations = {
            "plants_by_plant_type": {"terms": {"field": "plantTypeName"}},
            "plants_by_species": {"terms": {"field": "speciesName"}},
            "plants_by_variety": {"terms": {"field": "varietyName"}},
            "avg_price": {"avg": {"field": "price"}}
        }

        response = self.es.search(
            index=self.index,
            query=search_query,
            aggs=aggregations,
            sort=[{sort_by: {"order": order}}]
        )
        
        return {
            "hits": [hit["_source"] for hit in response["hits"]["hits"]],
            "aggregations": response.get("aggregations", {})
        }
