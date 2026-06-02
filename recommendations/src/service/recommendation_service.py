import time

from fastapi import HTTPException
from src.db import Neo4jDB
from src.repository.recommendation_repo import RecommendationRepository


class RecommendationService:
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
                time.strftime("%Y-%m-%dT%H:%M:%S+%z"),
            )

        if not records:
            raise HTTPException(status_code=404, detail="Customer or plant not found")

        return {"status": "created" if summary.counters.relationships_created > 0 else "updated"}

    @staticmethod
    def create_customer_like(customer_id: int, plant_id: int):
        with Neo4jDB.driver.session() as session:
            records, summary = session.execute_write(
                RecommendationRepository.create_like,
                customer_id,
                plant_id,
                time.strftime("%Y-%m-%dT%H:%M:%S+%z"),
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
                time.strftime("%Y-%m-%dT%H:%M:%S+%z"),
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
                time.strftime("%Y-%m-%dT%H:%M:%S+%z"),
                payload.quantity,
            )

        if not records:
            raise HTTPException(status_code=404, detail="Customer or plant not found")

        return {"status": "created" if summary.counters.relationships_created > 0 else "updated"}

    @staticmethod
    def get_general_recommendations():
        with Neo4jDB.driver.session() as session:
            return session.execute_read(RecommendationRepository.get_general_recommendations)

    @staticmethod
    def update_recommendation(recommendation_id: int, payload):
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
            return session.execute_read(RecommendationRepository.get_customer_recommendations, customer_id)
