from faker import Faker
import random
from datetime import datetime, timedelta
from src.core.elastic import es_client
from src.core.config import settings
from elasticsearch.helpers import bulk
import logging

logger = logging.getLogger(__name__)
fake = Faker()

PLANT_TYPES = ["Tree", "Flower", "Shrub", "Herb", "Cactus"]
SPECIES = ["Rosa", "Quercus", "Lavandula", "Acer", "Tulipa"]
VARIETIES = ["Red", "White", "Green", "Small", "Giant"]
REPORT_STATUSES = ["OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED"]

def generate_reports(user_id: int, num_reports: int):
    reports = []
    for i in range(num_reports):
        created_at = fake.date_time_between(start_date="-1y", end_date="now")
        resolved_at = None
        status = random.choice(REPORT_STATUSES)
        if status in ["RESOLVED", "CLOSED"]:
            resolved_at = created_at + timedelta(days=random.randint(1, 30))
        
        reports.append({
            "id": user_id * 1000 + i,
            "description": fake.paragraph(nb_sentences=3),
            "createdAt": created_at.isoformat(),
            "resolvedAt": resolved_at.isoformat() if resolved_at else None,
            "reportStatus": status,
            "offerId": random.randint(100, 10000),
            "plantId": random.randint(1, 1000),
            "plantName": fake.word().capitalize() + " " + random.choice(VARIETIES),
            "speciesName": random.choice(SPECIES),
            "plantTypeName": random.choice(PLANT_TYPES),
            "feedbackRating": random.randint(1, 5),
            "feedbackComment": fake.sentence()
        })
    return reports

def seed_data():
    # Check users count
    user_count = es_client.count(index=settings.USERS_INDEX)["count"]
    if user_count < settings.MIN_DOCS_FOR_SEEDING:
        logger.info(f"Seeding users... Current count: {user_count}")
        users = []
        for i in range(1, 1001):
            num_reports = random.randint(0, 5)
            reports = generate_reports(i, num_reports)
            user = {
                "_index": settings.USERS_INDEX,
                "_id": str(i),
                "id": i,
                "username": fake.user_name(),
                "firstName": fake.first_name(),
                "lastName": fake.last_name(),
                "email": fake.email(),
                "phoneNumber": fake.phone_number(),
                "address": fake.address().replace("\n", ", "),
                "city": fake.city(),
                "country": fake.country(),
                "zipCode": fake.zipcode(),
                "totalPurchases": random.randint(0, 50),
                "totalReports": num_reports,
                "createdAt": fake.date_time_between(start_date="-2y", end_date="now").isoformat(),
                "reports": reports
            }
            users.append(user)
            if len(users) >= 100:
                bulk(es_client, users)
                users = []
        if users:
            bulk(es_client, users)
        logger.info("Finished seeding users")

    # Check plants count
    plant_count = es_client.count(index=settings.PLANTS_INDEX)["count"]
    if plant_count < settings.MIN_DOCS_FOR_SEEDING:
        logger.info(f"Seeding plants... Current count: {plant_count}")
        plants = []
        for i in range(1, 1001):
            plant = {
                "_index": settings.PLANTS_INDEX,
                "_id": str(i),
                "id": i,
                "name": fake.word().capitalize() + " " + random.choice(VARIETIES),
                "description": fake.text(max_nb_chars=200),
                "varietyId": random.randint(1, 50),
                "varietyName": random.choice(VARIETIES),
                "speciesId": random.randint(1, 20),
                "speciesName": random.choice(SPECIES),
                "plantTypeId": random.randint(1, 10),
                "plantTypeName": random.choice(PLANT_TYPES),
                "price": round(random.uniform(5.0, 500.0), 2),
                "availability": random.randint(0, 100),
                "createdAt": fake.date_time_between(start_date="-2y", end_date="now").isoformat()
            }
            plants.append(plant)
            if len(plants) >= 100:
                bulk(es_client, plants)
                plants = []
        if plants:
            bulk(es_client, plants)
        logger.info("Finished seeding plants")
