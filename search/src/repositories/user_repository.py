from src.repositories.base import BaseRepository
from src.core.config import settings

class UserRepository(BaseRepository):
    def __init__(self, es):
        super().__init__(es, settings.USERS_INDEX)

    def search_users(
        self,
        query = None,
        city = None,
        country = None,
        min_purchases = None,
        min_reports = None,
        sort_by = "totalPurchases",
        order = "desc",
        page = 1,
        page_size = 10,
    ):
        must = []
        should = []

        if query:
            should.append({
                "prefix": {
                    "username": query
                }
            })

            should.append({
                "multi_match": {
                    "query": query,
                    "fields": [
                        "username^8",
                        "email^4",
                        "firstName^3",
                        "lastName^3"
                    ],
                    "type": "best_fields",
                    "fuzziness": "AUTO"
                }
            })

            should.append({
                "multi_match": {
                    "query": query,
                    "fields": [
                        "firstName.ngram^2",
                        "lastName.ngram^2"
                    ],
                    "type": "best_fields"
                }
            })
        else:
            must.append({"match_all": {}})

        filters = []

        if city:
            filters.append({"match": {"city": city}})
        if country:
            filters.append({"match": {"country": country}})
        if min_purchases is not None:
            filters.append({"range": {"totalPurchases": {"gte": min_purchases}}})
        if min_reports is not None:
            filters.append({"range": {"totalReports": {"gte": min_reports}}})

        search_query = {
            "bool": {
                "must": must,
                "should": should,
                "minimum_should_match": 1 if should else 0,
                "filter": filters
            }
        }

        from_ = (int(page) - 1) * int(page_size)

        response = self.es.search(
            index=self.index,
            query=search_query,
            sort=[{sort_by: {"order": order}}],
            from_=from_,
            size=page_size
        )
        
        return {
            "hits": [hit["_source"] for hit in response["hits"]["hits"]],
            "pagination": {
                "page": int(page),
                "page_size": int(page_size)
            }
        }

    def search_reports(
        self,
        query = None,
        status = None,
        min_rating = None,
        species = None,
        plant_type = None,
        date_from = None,
        date_to = None,
        sort_by = "createdAt",
        order = "desc"
    ):
        nested_must = []
        if query:
            nested_must.append({"match": {"reports.description": query}})
        
        nested_filters = []
        if status:
            nested_filters.append({"term": {"reports.reportStatus": status}})
        if min_rating:
            nested_filters.append({"range": {"reports.feedbackRating": {"gte": min_rating}}})
        if date_from or date_to:
            range_filter = {}
            if date_from: range_filter["gte"] = date_from
            if date_to: range_filter["lte"] = date_to
            nested_filters.append({"range": {"reports.createdAt": range_filter}})

        if species:
            nested_filters.append({"term": {"reports.speciesName": species}})
        if plant_type:
            nested_filters.append({"term": {"reports.plantTypeName": plant_type}})

        search_query = {
            "nested": {
                "path": "reports",
                "query": {
                    "bool": {
                        "must": nested_must,
                        "filter": nested_filters
                    }
                },
                "inner_hits": {
                    "sort": [{f"reports.{sort_by}": {"order": order}}]
                }
            }
        }

        aggregations = {
            "reports_nested": {
                "nested": {"path": "reports"},
                "aggs": {
                    "reports_by_status": {"terms": {"field": "reports.reportStatus"}},
                    "reports_by_species": {"terms": {"field": "reports.speciesName"}},
                    "reports_by_plant_type": {"terms": {"field": "reports.plantTypeName"}},
                    "avg_feedback_rating": {"avg": {"field": "reports.feedbackRating"}}
                }
            }
        }

        response = self.es.search(
            index=self.index,
            query=search_query,
            aggs=aggregations
        )

        results = []
        for hit in response["hits"]["hits"]:
            if "inner_hits" in hit:
                for report_hit in hit["inner_hits"]["reports"]["hits"]["hits"]:
                    results.append(report_hit["_source"])

        return {
            "hits": results,
            "aggregations": response.get("aggregations", {}).get("reports_nested", {})
        }
