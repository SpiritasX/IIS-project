USER_MAPPING = {
    "mappings": {
        "properties": {
            "id": {"type": "long"},
            "username": {"type": "keyword"},
            "firstName": {
                "type": "text",
                "fields": {"keyword": {"type": "keyword"}}
            },
            "lastName": {
                "type": "text",
                "fields": {"keyword": {"type": "keyword"}}
            },
            "email": {"type": "keyword"},
            "phoneNumber": {"type": "keyword"},
            "address": {"type": "text"},
            "city": {"type": "keyword"},
            "country": {"type": "keyword"},
            "zipCode": {"type": "keyword"},
            "totalPurchases": {"type": "integer"},
            "totalReports": {"type": "integer"},
            "createdAt": {"type": "date"},
            "reports": {
                "type": "nested",
                "properties": {
                    "id": {"type": "long"},
                    "description": {"type": "text"},
                    "createdAt": {"type": "date"},
                    "resolvedAt": {"type": "date"},
                    "reportStatus": {"type": "keyword"},
                    "offerId": {"type": "long"},
                    "plantId": {"type": "long"},
                    "plantName": {"type": "keyword"},
                    "speciesName": {"type": "keyword"},
                    "plantTypeName": {"type": "keyword"},
                    "feedbackRating": {"type": "integer"},
                    "feedbackComment": {"type": "text"}
                }
            }
        }
    }
}

PLANT_MAPPING = {
    "mappings": {
        "properties": {
            "id": {"type": "long"},
            "priceId": {"type": "long"},
            "name": {
                "type": "text",
                "fields": {"keyword": {"type": "keyword"}}
            },
            "description": {"type": "text"},
            "varietyId": {"type": "long"},
            "varietyName": {"type": "keyword"},
            "speciesId": {"type": "long"},
            "speciesName": {"type": "keyword"},
            "plantTypeId": {"type": "long"},
            "plantTypeName": {"type": "keyword"},
            "price": {"type": "double"},
            "availability": {"type": "integer"},
            "createdAt": {"type": "date"}
        }
    }
}
