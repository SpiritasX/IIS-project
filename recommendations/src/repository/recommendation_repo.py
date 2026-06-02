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
    def create_plant(tx, plant_id: int, name: str):
        result = tx.run(
            """
            MERGE (p:Plant {id: $plant_id})
            ON CREATE SET p.name = $name
            ON MATCH SET p.name = $name
            RETURN p
            """,
            plant_id=plant_id,
            name=name,
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
    def create_view(tx, customer_id: int, plant_id: int, timestamp: str):
        result = tx.run(
            """
            MATCH (c:Customer {id: $customer_id})
            MATCH (p:Plant {id: $plant_id})
            CREATE (c)-[rel:VIEWED {
                timestamp: datetime($timestamp)
            }]->(p)
            RETURN rel
            """,
            customer_id=customer_id,
            plant_id=plant_id,
            timestamp=timestamp,
        )
        return result.data(), result.consume()


    @staticmethod
    def create_like(tx, customer_id: int, plant_id: int, timestamp: str):
        result = tx.run(
            """
            MATCH (c:Customer {id: $customer_id})
            MATCH (p:Plant {id: $plant_id})
            MERGE (c)-[rel:LIKED]->(p)
            ON CREATE SET rel.timestamp = datetime($timestamp)
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
                query: $query,
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
            query=query,
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
            MERGE (c)-[rel:PURCHASES]->(p)
            SET rel.timestamp = datetime($timestamp),
                rel.quantity = $quantity
            RETURN rel
            """,
            customer_id=customer_id,
            plant_id=plant_id,
            timestamp=timestamp,
            quantity=quantity,
        )
        return result.data(), result.consume()

    @staticmethod
    def get_general_recommendations(tx):
        result = tx.run(
            """
            MATCH (p:Plant)

            OPTIONAL MATCH ()-[v_recent:VIEWED]->(p)
            WHERE v_recent.timestamp >= datetime() - duration('P30D')

            OPTIONAL MATCH ()-[v_prev:VIEWED]->(p)
            WHERE v_prev.timestamp >= datetime() - duration('P60D')
            AND v_prev.timestamp < datetime() - duration('P30D')

            OPTIONAL MATCH ()-[l_recent:LIKED]->(p)
            WHERE l_recent.timestamp >= datetime() - duration('P30D')

            OPTIONAL MATCH ()-[l_prev:LIKED]->(p)
            WHERE l_prev.timestamp >= datetime() - duration('P60D')
            AND l_prev.timestamp < datetime() - duration('P30D')

            OPTIONAL MATCH ()-[p_recent:PURCHASES]->(p)
            WHERE p_recent.timestamp >= datetime() - duration('P30D')

            OPTIONAL MATCH ()-[p_prev:PURCHASES]->(p)
            WHERE p_prev.timestamp >= datetime() - duration('P60D')
            AND p_prev.timestamp < datetime() - duration('P30D')

            WITH p,
                count(DISTINCT v_recent) - count(DISTINCT v_prev) AS views_growth,
                count(DISTINCT l_recent) - count(DISTINCT l_prev) AS likes_growth,
                count(DISTINCT p_recent) - count(DISTINCT p_prev) AS purchases_growth

            WITH p,
                (2 * views_growth) +
                (3 * likes_growth) +
                (5 * purchases_growth) AS score

            RETURN
                p.id AS id,
                p.name AS name,
                score
            ORDER BY score DESC, p.id ASC
            LIMIT 20
            """
        )
        return result.data()


    @staticmethod
    def get_customer_recommendations(tx, customer_id: int):
        result = tx.run(
            """
            MATCH (c:Customer {id: $customer_id})

            OPTIONAL MATCH (c)-[:PURCHASES]->(bought:Plant)
            WITH c, collect(DISTINCT bought) AS bought_plants

            MATCH (popular:Plant)
            WHERE NOT popular IN bought_plants

            OPTIONAL MATCH (:Customer)-[pur:PURCHASES]->(popular)
            WITH c, popular, count(pur) AS purchase_count
            ORDER BY purchase_count DESC, popular.id ASC
            LIMIT 20

            MERGE (c)-[r:RECOMMENDED]->(popular)
            ON CREATE SET
                r.id = randomUUID(),
                r.created_at = datetime(),
                r.viewed = false,
                r.purchased = false

            RETURN
                r.id AS recommendation_id,
                popular.id AS id,
                popular.name AS name,
                popular.seasonality AS seasonality,
                purchase_count AS score
            """,
            customer_id=customer_id,
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
