from src.repositories.base import BaseRepository
from src.core.config import settings

class PlantRepository(BaseRepository):
    def __init__(self, es):
        super().__init__(es, settings.PLANTS_INDEX)

    SORT_FIELDS = {
        "price": "price",
        "name": "name.keyword",
        "createdAt": "createdAt",
        "plantTypeName": "plantTypeName",
        "speciesName": "speciesName",
        "varietyName": "varietyName",
    }

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
        order: str = "asc",
        page: int = 1,
        page_size: int = 100
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
            filters.append({"term": {"plantTypeName": {"value": plant_type, "case_insensitive": True}}})
        if species:
            filters.append({"term": {"speciesName": {"value": species, "case_insensitive": True}}})
        if variety:
            filters.append({"term": {"varietyName": {"value": variety, "case_insensitive": True}}})
        
        price_range = {}
        normalized_min_price = self._optional_float(min_price)
        normalized_max_price = self._optional_float(max_price)
        if normalized_min_price is not None:
            price_range["gte"] = normalized_min_price
        if normalized_max_price is not None:
            price_range["lte"] = normalized_max_price
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

        normalized_page = self._bounded_int(page, default=1, minimum=1, maximum=1000)
        normalized_page_size = self._bounded_int(page_size, default=100, minimum=1, maximum=500)
        sort_field = self.SORT_FIELDS.get(sort_by, "price")
        sort_order = "desc" if str(order).lower() == "desc" else "asc"

        response = self.es.search(
            index=self.index,
            query=search_query,
            aggs=aggregations,
            from_=(normalized_page - 1) * normalized_page_size,
            size=normalized_page_size,
            sort=[{sort_field: {"order": sort_order}}],
            track_total_hits=True
        )
        total = response["hits"]["total"]
        
        return {
            "hits": [hit["_source"] for hit in response["hits"]["hits"]],
            "total": total["value"] if isinstance(total, dict) else total,
            "page": normalized_page,
            "page_size": normalized_page_size,
            "aggregations": response.get("aggregations", {})
        }

    def _optional_float(self, value):
        if value in (None, ""):
            return None

        try:
            return float(value)
        except (TypeError, ValueError):
            return None

    def _bounded_int(self, value, default: int, minimum: int, maximum: int):
        try:
            numeric_value = int(value)
        except (TypeError, ValueError):
            numeric_value = default

        return max(minimum, min(maximum, numeric_value))
