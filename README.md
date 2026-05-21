# IIS Project

## Prerequisites
- [Docker](https://www.docker.com/) installed

## Quick links
  - [Frontend](http://localhost:5173)
  - [Postgres dashboard](http://localhost:8090)
    - Email: admin@example.com
    - Password: admin
  - [Neo4j dashboard](http://localhost:7474)
    - Username: neo4j
    - Password: password

---

## Run the project

From the project root (where `docker-compose.yaml` is located):

```bash
docker compose up --build
```

## Stop the project
```bash
docker compose down
```

## Reset database data
```bash
docker compose down -v
```