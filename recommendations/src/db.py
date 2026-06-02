from fastapi import HTTPException
from neo4j import GraphDatabase
from neo4j.exceptions import Neo4jError


class Neo4jDB:
    driver = GraphDatabase.driver(
        "bolt://neo4j:7687",
        auth=("neo4j", "password")
    )

    @staticmethod
    def run_query(query: str, **params):
        try:
            records, summary, _ = Neo4jDB.driver.execute_query(query, **params)
            return records, summary
        except Neo4jError as e:
            raise HTTPException(status_code=500, detail=str(e))


driver = Neo4jDB.driver
run_query = Neo4jDB.run_query