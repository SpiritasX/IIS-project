import time

from fastapi import HTTPException
from src.db import Neo4jDB
from src.repository.recommendation_repo import RecommendationRepository


class RecommendationService:
    RECENT_RECOMMENDATION_STATUSES = {
        "all",
        "successful",
        "unsuccessful",
        "viewed",
        "liked",
        "purchased",
    }

    @staticmethod
    def create_customer(payload):
        with Neo4jDB.driver.session() as session:
            records, summary = session.execute_write(
                RecommendationRepository.create_customer,
                payload.id,
                payload.first_name,
                payload.last_name,
            )

        if not records:
            raise HTTPException(status_code=500, detail="Customer not created")

        return {"status": "created" if summary.counters.nodes_created > 0 else "updated"}

    @staticmethod
    def delete_customer(customer_id: int):
        with Neo4jDB.driver.session() as session:
            summary = session.execute_write(RecommendationRepository.delete_customer, customer_id)

        if summary.counters.nodes_deleted == 0:
            raise HTTPException(status_code=404, detail="Customer not found")

        return {"status": "deleted"}
    
    @staticmethod
    def create_customer_view(customer_id: int, plant_id: int):
        with Neo4jDB.driver.session() as session:
            records, summary = session.execute_write(
                RecommendationRepository.create_view,
                customer_id,
                plant_id,
                time.strftime("%Y-%m-%dT%H:%M:%S%z"),
            )

        if not records:
            raise HTTPException(status_code=404, detail="Customer or plant not found")

        return {"status": "created" if summary.counters.relationships_created > 0 else "updated"}

    @staticmethod
    def get_customer_like(customer_id: int, plant_id: int):
        with Neo4jDB.driver.session() as session:
            records, summary = session.execute_read(
                RecommendationRepository.get_customer_like,
                customer_id,
                plant_id
            )

        if not records:
            raise HTTPException(status_code=404, detail="Customer or plant not found")

        if len(records) == 0:
            raise HTTPException(status_code=404, detail="Customer not liked plant")

        return {"status": "liked"}


    @staticmethod
    def create_customer_like(customer_id: int, plant_id: int):
        with Neo4jDB.driver.session() as session:
            records, summary = session.execute_write(
                RecommendationRepository.create_like,
                customer_id,
                plant_id,
                time.strftime("%Y-%m-%dT%H:%M:%S%z"),
            )

        if not records:
            raise HTTPException(status_code=404, detail="Customer or plant not found")

        return {"status": "created" if summary.counters.relationships_created > 0 else "updated"}
    
    @staticmethod
    def unlike_customer_like(customer_id: int, plant_id: int):
        with Neo4jDB.driver.session() as session:
            summary = session.execute_write(
                RecommendationRepository.delete_like,
                customer_id,
                plant_id,
            )

        if summary.counters.relationships_deleted == 0:
            raise HTTPException(status_code=404, detail="Like relationship not found")

        return {"status": "deleted"}

    @staticmethod
    def create_plant(payload):
        with Neo4jDB.driver.session() as session:
            records, summary = session.execute_write(
                RecommendationRepository.create_plant,
                payload.id,
                payload.name,
                payload.plant_variety_id,
            )

        if not records:
            raise HTTPException(status_code=500, detail="Plant not created")

        return {"status": "created" if summary.counters.nodes_created > 0 else "updated"}

    @staticmethod
    def delete_plant(plant_id: int):
        with Neo4jDB.driver.session() as session:
            summary = session.execute_write(RecommendationRepository.delete_plant, plant_id)

        if summary.counters.nodes_deleted == 0:
            raise HTTPException(status_code=404, detail="Plant not found")

        return {"status": "deleted"}

    @staticmethod
    def create_plant_variety(payload):
        with Neo4jDB.driver.session() as session:
            records, summary = session.execute_write(
                RecommendationRepository.create_plant_variety,
                payload.id,
                payload.name,
                payload.season,
            )

        if not records:
            raise HTTPException(status_code=500, detail="Plant variety not created")

        return {"status": "created" if summary.counters.nodes_created > 0 else "updated"}

    @staticmethod
    def delete_plant_variety(plant_variety_id: int):
        with Neo4jDB.driver.session() as session:
            summary = session.execute_write(RecommendationRepository.delete_plant_variety, plant_variety_id)

        if summary.counters.nodes_deleted == 0:
            raise HTTPException(status_code=404, detail="Plant variety not found")

        return {"status": "deleted"}

    @staticmethod
    def create_search(payload):
        with Neo4jDB.driver.session() as session:
            records, summary = session.execute_write(
                RecommendationRepository.create_search,
                payload.customer_id,
                payload.query,
                payload.min_price,
                payload.max_price,
                payload.variety,
                payload.species,
                payload.type,
                time.strftime("%Y-%m-%dT%H:%M:%S%z"),
            )

        if not records:
            raise HTTPException(status_code=404, detail="Customer not found")

        return {"status": "created"}

    @staticmethod
    def create_purchase(payload):
        with Neo4jDB.driver.session() as session:
            records, summary = session.execute_write(
                RecommendationRepository.create_purchase,
                payload.customer_id,
                payload.plant_id,
                time.strftime("%Y-%m-%dT%H:%M:%S%z"),
                payload.quantity,
            )

        if not records:
            raise HTTPException(status_code=404, detail="Customer or plant not found")

        return {"status": "created" if summary.counters.relationships_created > 0 else "updated"}

    @staticmethod
    def get_general_recommendations():
        with Neo4jDB.driver.session() as session:
            return session.execute_read(RecommendationRepository.get_trending_seasonal_plants)

    @staticmethod
    def get_trending_recommendations():
        with Neo4jDB.driver.session() as session:
            return session.execute_read(RecommendationRepository.get_trending_plants)

    @staticmethod
    def get_seasonal_recommendations():
        with Neo4jDB.driver.session() as session:
            return session.execute_read(RecommendationRepository.get_trending_seasonal_plants)

    @staticmethod
    def update_recommendation(recommendation_id: str, payload):
        with Neo4jDB.driver.session() as session:
            records, summary = session.execute_write(
                RecommendationRepository.update_recommendation,
                recommendation_id,
                payload.viewed,
                payload.purchased,
            )

        if not records:
            raise HTTPException(status_code=404, detail="Recommendation not found")

        return {"status": "updated"}

    @staticmethod
    def get_customer_recommendations(customer_id: int):
        with Neo4jDB.driver.session() as session:
            return session.execute_write(RecommendationRepository.get_customer_recommendations, customer_id)

    @staticmethod
    def get_recent_recommendations(status: str = "all", limit: int = 25):
        normalized_status = RecommendationService._normalize_status(status)
        normalized_limit = RecommendationService._normalize_limit(limit, 1, 100)

        with Neo4jDB.driver.session() as session:
            return session.execute_read(
                RecommendationRepository.get_recent_recommendations,
                normalized_status,
                normalized_limit,
            )

    @staticmethod
    def get_recommendation_effectiveness(limit: int = 10):
        normalized_limit = RecommendationService._normalize_limit(limit, 1, 50)

        with Neo4jDB.driver.session() as session:
            kpis = session.execute_read(RecommendationRepository.get_recommendation_kpis)
            success_paths = session.execute_read(RecommendationRepository.get_recommendation_success_paths)
            top_plants = session.execute_read(
                RecommendationRepository.get_top_recommended_plants,
                normalized_limit,
            )
            weekday_histogram = session.execute_read(RecommendationRepository.get_recommendations_by_day)

        summary = kpis[0] if kpis else {}
        total = summary.get("total", 0) or 0

        return {
            "summary": {
                **summary,
                "success_rate": RecommendationService._rate(summary.get("successful", 0), total),
                "view_rate": RecommendationService._rate(summary.get("viewed", 0), total),
                "like_rate": RecommendationService._rate(summary.get("liked", 0), total),
                "purchase_rate": RecommendationService._rate(summary.get("purchased", 0), total),
            },
            "success_paths": RecommendationService._with_share(success_paths, total),
            "top_plants": top_plants,
            "weekday_histogram": weekday_histogram,
        }

    @staticmethod
    def _normalize_status(status: str | None):
        normalized_status = (status or "all").strip().lower()

        if normalized_status not in RecommendationService.RECENT_RECOMMENDATION_STATUSES:
            raise HTTPException(status_code=400, detail="Invalid recommendation status")

        return normalized_status

    @staticmethod
    def _normalize_limit(limit: int | str | None, minimum: int, maximum: int):
        try:
            numeric_limit = int(limit)
        except (TypeError, ValueError):
            raise HTTPException(status_code=400, detail="Invalid limit")

        return max(minimum, min(maximum, numeric_limit))

    @staticmethod
    def _rate(value: int | float | None, total: int | float | None):
        if not total:
            return 0.0

        return round(float(value or 0) / float(total), 4)

    @staticmethod
    def _with_share(records, total: int):
        return [
            {
                **record,
                "share": RecommendationService._rate(record.get("count", 0), total),
            }
            for record in records
        ]
