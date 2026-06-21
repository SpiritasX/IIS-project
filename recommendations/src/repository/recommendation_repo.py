class RecommendationRepository:
    @staticmethod
    def create_customer(tx, customer_id: int, first_name: str, last_name: str):
        result = tx.run(
            """
            MERGE (c:Customer {id: $customer_id})
            ON CREATE SET c.first_name = $first_name, c.last_name = $last_name
            ON MATCH SET c.first_name = $first_name, c.last_name = $last_name
            RETURN c
            """,
            customer_id=customer_id,
            first_name=first_name,
            last_name=last_name,
        )
        return result.data(), result.consume()

    @staticmethod
    def delete_customer(tx, customer_id: int):
        result = tx.run(
            """
            MATCH (c:Customer {id: $customer_id})
            DETACH DELETE c
            """,
            customer_id=customer_id,
        )
        return result.consume()

    @staticmethod
    def create_plant(tx, plant_id: int, name: str, plant_variety_id: int):
        result = tx.run(
            """
            MERGE (p:Plant {id: $plant_id})
            ON CREATE SET p.name = $name
            ON MATCH SET p.name = $name
            WITH p
            MATCH (pv:PlantVariety {id: $plant_variety_id})
            MERGE (p)-[:PLANT_VARIETY]->(pv)
            RETURN p
            """,
            plant_id=plant_id,
            name=name,
            plant_variety_id=plant_variety_id,
        )
        return result.data(), result.consume()

    @staticmethod
    def delete_plant(tx, plant_id: int):
        result = tx.run(
            """
            MATCH (p:Plant {id: $plant_id})
            DETACH DELETE p
            """,
            plant_id=plant_id,
        )
        return result.consume()

    @staticmethod
    def create_plant_variety(tx, id: int, name: str, season: str):
        result = tx.run(
            """
            MERGE (p:PlantVariety {id: $id})
            ON CREATE SET p.name = $name, p.season = $season
            ON MATCH SET p.name = $name, p.season = $season
            RETURN p
            """,
            id=id,
            name=name,
            season=season,
        )
        return result.data(), result.consume()

    @staticmethod
    def delete_plant_variety(tx, plant_variety_id: int):
        result = tx.run(
            """
            MATCH (pv:PlantVariety {id: $plant_variety_id})
            DETACH DELETE pv
            """,
            plant_variety_id=plant_variety_id,
        )
        return result.consume()
    
    @staticmethod
    def create_view(tx, customer_id: int, plant_id: int, timestamp: str):
        result = tx.run(
            """
            MATCH (c:Customer {id: $customer_id})
            MATCH (p:Plant {id: $plant_id})
            OPTIONAL MATCH (c)-[recommendation:RECOMMENDED]->(p)
            MERGE (c)-[rel:VIEWED]->(p)
            ON CREATE SET
                rel.created_at = datetime($timestamp),
                rel.updated_at = datetime($timestamp),
                rel.count = 1
            ON MATCH SET
                rel.updated_at = datetime($timestamp),
                rel.count = rel.count + 1
            FOREACH (_ IN CASE WHEN recommendation IS NULL THEN [] ELSE [1] END |
                SET recommendation.viewed = true,
                    recommendation.successful = true,
                    recommendation.viewed_at = datetime($timestamp)
            )
            RETURN rel
            """,
            customer_id=customer_id,
            plant_id=plant_id,
            timestamp=timestamp,
        )
        return result.data(), result.consume()


    @staticmethod
    def get_customer_like(tx, customer_id: int, plant_id: int):
        result = tx.run(
            """
            MATCH (c:Customer {id: $customer_id})-[rel:LIKED]->(p:Plant {id: $plant_id})
            RETURN rel
            """,
            customer_id=customer_id,
            plant_id=plant_id,
        )
        return result.data(), result.consume()


    @staticmethod
    def create_like(tx, customer_id: int, plant_id: int, timestamp: str):
        result = tx.run(
            """
            MATCH (c:Customer {id: $customer_id})
            MATCH (p:Plant {id: $plant_id})
            OPTIONAL MATCH (c)-[recommendation:RECOMMENDED]->(p)
    
            MERGE (c)-[rel:LIKED]->(p)
            ON CREATE SET rel.timestamp = datetime($timestamp)
    
            FOREACH (_ IN CASE WHEN recommendation IS NULL THEN [] ELSE [1] END |
                SET recommendation.liked = true,
                    recommendation.successful = true,
                    recommendation.liked_at = datetime($timestamp)
            )
    
            RETURN rel
            """,
            customer_id=customer_id,
            plant_id=plant_id,
            timestamp=timestamp,
        )
        return result.data(), result.consume()


    @staticmethod
    def delete_like(tx, customer_id: int, plant_id: int):
        result = tx.run(
            """
            MATCH (c:Customer {id: $customer_id})-[rel:LIKED]->(p:Plant {id: $plant_id})
            DELETE rel
            """,
            customer_id=customer_id,
            plant_id=plant_id,
        )
        return result.consume()

    @staticmethod
    def create_search(tx, customer_id: int, query: str | None, min_price: int | None, max_price: int | None, variety: str | None, species: str | None, type: str | None, timestamp: str):
        result = tx.run(
            """
            MATCH (c:Customer {id: $customer_id})
            CREATE (s:Search {
                query: $search_query,
                min_price: $min_price,
                max_price: $max_price,
                variety: $variety,
                species: $species,
                type: $type,
                timestamp: datetime($timestamp)
            })
            MERGE (c)-[:SEARCHES]->(s)
            RETURN s
            """,
            customer_id=customer_id,
            search_query=query,
            min_price=min_price,
            max_price=max_price,
            variety=variety,
            species=species,
            type=type,
            timestamp=timestamp,
        )
        return result.data(), result.consume()

    @staticmethod
    def create_purchase(tx, customer_id: int, plant_id: int, timestamp: str, quantity: int):
        result = tx.run(
            """
            MATCH (c:Customer {id: $customer_id})
            MATCH (p:Plant {id: $plant_id})
            OPTIONAL MATCH (c)-[recommendation:RECOMMENDED]->(p)
            MERGE (c)-[rel:PURCHASES]->(p)
            SET rel.timestamp = datetime($timestamp),
                rel.quantity = $quantity
            FOREACH (_ IN CASE WHEN recommendation IS NULL THEN [] ELSE [1] END |
                SET recommendation.purchased = true,
                    recommendation.successful = true,
                    recommendation.purchased_at = datetime($timestamp)
            )
            RETURN rel
            """,
            customer_id=customer_id,
            plant_id=plant_id,
            timestamp=timestamp,
            quantity=quantity,
        )
        return result.data(), result.consume()

    @staticmethod
    def get_trending_seasonal_plants(tx):
        result = tx.run(
            """
            MATCH (p:Plant)-[:PLANT_VARIETY]->(:PlantVariety { 
                season: CASE
                    WHEN datetime().month IN [3, 4] THEN 'SPRING'
                    WHEN datetime().month IN [5, 6, 7, 8] THEN 'SUMMER'
                    WHEN datetime().month IN [9, 10] THEN 'FALL'
                    ELSE 'WINTER'
                END
            })

            CALL (p) {
                OPTIONAL MATCH ()-[v_recent:VIEWED]->(p)
                WHERE v_recent.updated_at >= datetime() - duration('P30D')
                RETURN coalesce(sum(v_recent.count), 0) AS recent_views
            }

            CALL (p) {
                OPTIONAL MATCH ()-[v_prev:VIEWED]->(p)
                WHERE v_prev.updated_at >= datetime() - duration('P60D')
                  AND v_prev.updated_at < datetime() - duration('P30D')
                RETURN coalesce(sum(v_prev.count), 0) AS prev_views
            }

            CALL (p) {
                OPTIONAL MATCH ()-[l:LIKED]->(p)
                RETURN coalesce(count(DISTINCT l), 0) AS likes
            }

            CALL (p) {
                OPTIONAL MATCH ()-[p_recent:PURCHASES]->(p)
                WHERE p_recent.timestamp >= datetime() - duration('P30D')
                RETURN coalesce(sum(p_recent.quantity), 0) AS recent_purchases
            }

            CALL (p) {
                OPTIONAL MATCH ()-[p_prev:PURCHASES]->(p)
                WHERE p_prev.timestamp >= datetime() - duration('P60D')
                  AND p_prev.timestamp < datetime() - duration('P30D')
                RETURN coalesce(sum(p_prev.quantity), 0) AS prev_purchases
            }

            WITH p,
                recent_views - prev_views AS views_growth,
                likes AS likes,
                recent_purchases - prev_purchases AS purchases_growth

            WITH p,
                (2 * views_growth) +
                (3 * likes) +
                (5 * purchases_growth) AS score

            RETURN
                p.id AS id,
                p.name AS name,
                score
            ORDER BY score DESC, p.id ASC
            LIMIT 5
            """
        )
        return result.data()

    @staticmethod
    def get_trending_plants(tx):
        result = tx.run(
            """
            MATCH (p:Plant)

            CALL (p) {
                OPTIONAL MATCH ()-[v_recent:VIEWED]->(p)
                WHERE v_recent.updated_at >= datetime() - duration('P30D')
                RETURN coalesce(sum(v_recent.count), 0) AS recent_views
            }

            CALL (p) {
                OPTIONAL MATCH ()-[v_prev:VIEWED]->(p)
                WHERE v_prev.updated_at >= datetime() - duration('P60D')
                  AND v_prev.updated_at < datetime() - duration('P30D')
                RETURN coalesce(sum(v_prev.count), 0) AS prev_views
            }

            CALL (p) {
                OPTIONAL MATCH ()-[l:LIKED]->(p)
                RETURN coalesce(count(DISTINCT l), 0) AS likes
            }

            CALL (p) {
                OPTIONAL MATCH ()-[p_recent:PURCHASES]->(p)
                WHERE p_recent.timestamp >= datetime() - duration('P30D')
                RETURN coalesce(sum(p_recent.quantity), 0) AS recent_purchases
            }

            CALL (p) {
                OPTIONAL MATCH ()-[p_prev:PURCHASES]->(p)
                WHERE p_prev.timestamp >= datetime() - duration('P60D')
                  AND p_prev.timestamp < datetime() - duration('P30D')
                RETURN coalesce(sum(p_prev.quantity), 0) AS prev_purchases
            }

            WITH p,
                recent_views - prev_views AS views_growth,
                likes AS likes,
                recent_purchases - prev_purchases AS purchases_growth

            WITH p,
                (2 * views_growth) +
                (3 * likes) +
                (5 * purchases_growth) AS score

            RETURN
                p.id AS id,
                p.name AS name,
                score
            ORDER BY score DESC, p.id ASC
            LIMIT 5
            """
        )
        return result.data()


    @staticmethod
    def get_customer_recommendations(tx, customer_id: int):
        result = tx.run(
            """
            MATCH (c:Customer {id: $customer_id})
    
            CALL (c) {
                OPTIONAL MATCH (c)-[v:VIEWED]->(viewed:Plant)
                RETURN
                    collect(
                        CASE
                            WHEN v IS NULL THEN NULL
                            ELSE {
                                plant: viewed,
                                weight: toFloat(coalesce(v.count, 1)) * 1.0
                            }
                        END
                    ) AS viewed_interactions,
                    count(v) AS view_count
            }
    
            CALL (c) {
                OPTIONAL MATCH (c)-[l:LIKED]->(liked:Plant)
                RETURN
                    collect(
                        CASE
                            WHEN l IS NULL THEN NULL
                            ELSE {
                                plant: liked,
                                weight: 4.0
                            }
                        END
                    ) AS liked_interactions,
                    count(l) AS like_count
            }
    
            CALL (c) {
                OPTIONAL MATCH (c)-[p:PURCHASES]->(purchased:Plant)
                RETURN
                    collect(
                        CASE
                            WHEN p IS NULL THEN NULL
                            ELSE {
                                plant: purchased,
                                weight: toFloat(coalesce(p.quantity, 1)) * 8.0
                            }
                        END
                    ) AS purchased_interactions,
                    collect(DISTINCT purchased) AS purchased_plants,
                    count(p) AS purchase_count
            }
    
            CALL (c) {
                OPTIONAL MATCH (c)-[:SEARCHES]->(s:Search)
                RETURN
                    collect(s) AS searches,
                    count(s) AS search_count
            }
    
            WITH
                c,
                viewed_interactions + liked_interactions + purchased_interactions AS interactions,
                purchased_plants,
                searches,
                view_count + like_count + purchase_count + search_count AS data_count
    
            // New users / users with no collected data get no personalized recommendations.
            WHERE data_count > 0
    
            MATCH (candidate:Plant)-[:PLANT_VARIETY]->(candidate_variety:PlantVariety)
            WHERE NOT candidate IN purchased_plants
    
            CALL (interactions, candidate, candidate_variety) {
                UNWIND interactions AS interaction
                WITH
                    interaction.plant AS interacted_plant,
                    interaction.weight AS interaction_weight,
                    candidate,
                    candidate_variety
                WHERE interacted_plant IS NOT NULL
    
                OPTIONAL MATCH (interacted_plant)-[:PLANT_VARIETY]->(interaction_variety:PlantVariety)
    
                RETURN coalesce(sum(
                    interaction_weight *
                    CASE
                        WHEN interaction_variety = candidate_variety THEN 1.0
                        WHEN interaction_variety.season = candidate_variety.season THEN 0.25
                        ELSE 0.0
                    END
                ), 0.0) AS interaction_score
            }
    
            CALL {
                WITH searches, candidate, candidate_variety
                UNWIND searches AS s
                WITH s, candidate, candidate_variety
    
                RETURN coalesce(sum(
                    CASE
                        WHEN s.query IS NOT NULL
                         AND trim(s.query) <> ''
                         AND (
                            toLower(candidate.name) CONTAINS toLower(s.query)
                            OR toLower(candidate_variety.name) CONTAINS toLower(s.query)
                            OR toLower(s.query) CONTAINS toLower(candidate.name)
                            OR toLower(s.query) CONTAINS toLower(candidate_variety.name)
                         )
                        THEN 2.0
                        ELSE 0.0
                    END
                    +
                    CASE
                        WHEN s.variety IS NOT NULL
                         AND toLower(candidate_variety.name) = toLower(s.variety)
                        THEN 4.0
                        ELSE 0.0
                    END
                    +
                    CASE
                        WHEN s.species IS NOT NULL
                         AND toLower(coalesce(candidate.species, '')) = toLower(s.species)
                        THEN 3.0
                        ELSE 0.0
                    END
                    +
                    CASE
                        WHEN s.type IS NOT NULL
                         AND toLower(coalesce(candidate.type, '')) = toLower(s.type)
                        THEN 3.0
                        ELSE 0.0
                    END
                    +
                    CASE
                        WHEN (s.min_price IS NOT NULL OR s.max_price IS NOT NULL)
                         AND candidate.price IS NOT NULL
                         AND (s.min_price IS NULL OR candidate.price >= s.min_price)
                         AND (s.max_price IS NULL OR candidate.price <= s.max_price)
                        THEN 1.5
                        ELSE 0.0
                    END
                ), 0.0) AS search_score
            }
    
            CALL {
                WITH candidate
                OPTIONAL MATCH (:Customer)-[v:VIEWED]->(candidate)
                RETURN coalesce(sum(
                    CASE
                        WHEN v IS NULL THEN 0
                        ELSE coalesce(v.count, 1)
                    END
                ), 0) AS global_views
            }
    
            CALL {
                WITH candidate
                OPTIONAL MATCH (:Customer)-[l:LIKED]->(candidate)
                RETURN count(l) AS global_likes
            }
    
            CALL {
                WITH candidate
                OPTIONAL MATCH (:Customer)-[p:PURCHASES]->(candidate)
                RETURN coalesce(sum(
                    CASE
                        WHEN p IS NULL THEN 0
                        ELSE coalesce(p.quantity, 1)
                    END
                ), 0) AS global_purchases
            }
    
            WITH
                c,
                candidate,
                candidate_variety,
                interaction_score,
                search_score,
                (
                    interaction_score +
                    search_score
                ) AS personal_score,
                (
                    global_views * 0.05 +
                    global_likes * 0.25 +
                    global_purchases * 0.5
                ) AS popularity_tie_breaker
    
            // Prevent returning generic popular products as "personalized".
            WHERE personal_score > 0
    
            WITH
                c,
                candidate,
                candidate_variety,
                personal_score + popularity_tie_breaker AS score
    
            ORDER BY score DESC, candidate.id ASC
            LIMIT 5
    
            MERGE (c)-[r:RECOMMENDED]->(candidate)
            ON CREATE SET
                r.id = randomUUID(),
                r.created_at = datetime(),
                r.viewed = false,
                r.liked = false,
                r.purchased = false,
                r.successful = false
            ON MATCH SET
                r.viewed = coalesce(r.viewed, false),
                r.liked = coalesce(r.liked, false),
                r.purchased = coalesce(r.purchased, false),
                r.successful = coalesce(r.successful, false)
    
            RETURN
                r.id AS recommendation_id,
                candidate.id AS id,
                candidate.name AS name,
                candidate_variety.season AS seasonality,
                r.viewed AS viewed,
                r.liked AS liked,
                r.purchased AS purchased,
                r.successful AS successful,
                score
            """,
            customer_id=customer_id,
        )
        return result.data()

    @staticmethod
    def get_recent_recommendations(tx, status: str, limit: int):
        result = tx.run(
            """
            MATCH (c:Customer)-[r:RECOMMENDED]->(p:Plant)
            OPTIONAL MATCH (p)-[:PLANT_VARIETY]->(pv:PlantVariety)
            WHERE $status = 'all'
               OR ($status = 'successful' AND r.successful = true)
               OR ($status = 'unsuccessful' AND r.successful = false)
               OR ($status = 'viewed' AND r.viewed = true)
               OR ($status = 'liked' AND r.liked = true)
               OR ($status = 'purchased' AND r.purchased = true)
            RETURN
                r.id AS recommendation_id,
                c.id AS customer_id,
                c.first_name + ' ' + c.last_name AS customer_name,
                p.id AS plant_id,
                p.name AS plant_name,
                pv.name AS variety,
                pv.season AS season,
                toString(r.created_at) AS created_at,
                r.viewed AS viewed,
                r.liked AS liked,
                r.purchased AS purchased,
                r.successful AS successful
            ORDER BY r.created_at DESC, recommendation_id ASC
            LIMIT $limit
            """,
            status=status,
            limit=limit,
        )
        return result.data()

    @staticmethod
    def get_recommendation_kpis(tx):
        result = tx.run(
            """
            MATCH ()-[r:RECOMMENDED]->()
            RETURN
                count(r) AS total,
                sum(CASE WHEN r.successful THEN 1 ELSE 0 END) AS successful,
                sum(CASE WHEN r.viewed THEN 1 ELSE 0 END) AS viewed,
                sum(CASE WHEN r.liked THEN 1 ELSE 0 END) AS liked,
                sum(CASE WHEN r.purchased THEN 1 ELSE 0 END) AS purchased,
                sum(CASE WHEN NOT r.successful THEN 1 ELSE 0 END) AS unsuccessful
            """
        )
        return result.data()

    @staticmethod
    def get_recommendation_success_paths(tx):
        result = tx.run(
            """
            MATCH ()-[r:RECOMMENDED]->()
            WITH CASE
                WHEN r.purchased THEN 'Purchased'
                WHEN r.liked THEN 'Liked only'
                WHEN r.viewed THEN 'Viewed only'
                ELSE 'No interaction'
            END AS path
            RETURN path, count(*) AS count
            ORDER BY count DESC, path ASC
            """
        )
        return result.data()

    @staticmethod
    def get_top_recommended_plants(tx, limit: int):
        result = tx.run(
            """
            MATCH ()-[r:RECOMMENDED]->(p:Plant)
            OPTIONAL MATCH (p)-[:PLANT_VARIETY]->(pv:PlantVariety)
            WITH
                p,
                pv,
                count(r) AS recommendation_count,
                sum(CASE WHEN r.successful THEN 1 ELSE 0 END) AS successful_count,
                sum(CASE WHEN r.viewed THEN 1 ELSE 0 END) AS viewed_count,
                sum(CASE WHEN r.liked THEN 1 ELSE 0 END) AS liked_count,
                sum(CASE WHEN r.purchased THEN 1 ELSE 0 END) AS purchased_count
            RETURN
                p.id AS plant_id,
                p.name AS plant_name,
                pv.name AS variety,
                pv.season AS season,
                recommendation_count,
                successful_count,
                viewed_count,
                liked_count,
                purchased_count,
                CASE
                    WHEN recommendation_count = 0 THEN 0.0
                    ELSE toFloat(successful_count) / recommendation_count
                END AS success_rate
            ORDER BY successful_count DESC, success_rate DESC, recommendation_count DESC, plant_name ASC
            LIMIT $limit
            """,
            limit=limit,
        )
        return result.data()

    @staticmethod
    def get_recommendations_by_day(tx):
        result = tx.run(
            """
            UNWIND [
                {index: 1, name: 'Monday'},
                {index: 2, name: 'Tuesday'},
                {index: 3, name: 'Wednesday'},
                {index: 4, name: 'Thursday'},
                {index: 5, name: 'Friday'},
                {index: 6, name: 'Saturday'},
                {index: 7, name: 'Sunday'}
            ] AS weekday
            OPTIONAL MATCH ()-[r:RECOMMENDED]->()
            WHERE r.created_at IS NOT NULL
              AND date(r.created_at).dayOfWeek = weekday.index
            WITH
                weekday,
                count(r) AS recommendation_count,
                sum(CASE WHEN coalesce(r.successful, false) THEN 1 ELSE 0 END) AS successful_count
            RETURN
                weekday.index AS day_index,
                weekday.name AS day,
                recommendation_count,
                successful_count,
                CASE
                    WHEN recommendation_count = 0 THEN 0.0
                    ELSE toFloat(successful_count) / recommendation_count
                END AS success_rate
            ORDER BY day_index ASC
            """,
        )
        return result.data()


    @staticmethod
    def update_recommendation(tx, recommendation_id: str, viewed: bool | None, purchased: bool | None):
        result = tx.run(
            """
            MATCH ()-[r:RECOMMENDED]->()
            WHERE r.id = $recommendation_id
            SET r.viewed = coalesce($viewed, r.viewed),
                r.purchased = coalesce($purchased, r.purchased)
            RETURN r
            """,
            recommendation_id=recommendation_id,
            viewed=viewed,
            purchased=purchased,
        )
        return result.data(), result.consume()
