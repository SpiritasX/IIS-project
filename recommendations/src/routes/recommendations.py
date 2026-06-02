from fastapi import APIRouter
from src.db import driver, run_query

router = APIRouter(prefix="/recommendations")

# Users who like same categories, buy same plants
@router.get("/{user_id}/likeSameCategory")
def get_simple_recommendation(user_id: int):
    results, _ = run_query(
        """
            MATCH (u:User {user_id: $user_id})-[:LIKED]->(c:PlantCategory)<-[:LIKED]-(other:User)
            MATCH (other)-[]->(r:Request)<-[]-(:Transaction)
            MATCH (r)-[]->(p:Plant)
            WHERE NOT (u)-[:VIEWED]->(p) AND NOT (u)-[]->(r)-[]->(p)
            RETURN p.plant_id, count(other) AS score
            ORDER BY score DESC
            LIMIT 10
        """,
        user_id=user_id
    )

    return results

# Might want to consider weighing
# SUM(
#   CASE TYPE(ints)
#     WHEN 'LIKED' THEN 3
#     WHEN 'VIEWED' THEN 1
#     ELSE 0
#   END
# ) AS score
# Users who interact with same plants are interested in same plants
@router.get("/{user_id}/interactSamePlant")
def category_affinity(user_id: int):
    results, _ = run_query(
        """
            MATCH (u:User {user_id: $user_id})-[:VIEWED|LIKED]->(p:Plant)<-[:VIEWED|LIKED]-(other:User)
            WITH other, collect(DISTINCT p) as plants

            MATCH (other:User)-[ints:VIEWED|LIKED]->(p2:Plant)
            WHERE NOT p2 IN plants
            WITH p2, count(ints) AS score

            RETURN p2.plant_id AS plant_id, score
            ORDER BY score DESC
            LIMIT 10
        """,
        user_id=user_id
    )

    return results

# Counting views as view_counts is prone to spam
# Counting views as individual connections count(*) might be better
@router.get("/trending_plants")
def trending_plants():
    results, _ = run_query(
        """
            MATCH (:User)-[v:VIEWED]->(p:Plant)
            WITH p, sum(v.view_count) AS views

            MATCH (:Request)-[:CONTAINS]->(p)
            WITH p, views, count(*) AS request_score

            RETURN p.plant_id AS plant_id,
                (views + request_score) AS score
            ORDER BY score DESC
            LIMIT 10
        """,
    )

    return results