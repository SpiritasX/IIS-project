from elasticsearch import Elasticsearch

class BaseRepository:
    def __init__(self, es: Elasticsearch, index: str):
        self.es = es
        self.index = index

    def create(self, document_id, document):
        return self.es.index(index=self.index, id=str(document_id), document=document, refresh=True)

    def get_by_id(self, document_id):
        try:
            response = self.es.get(index=self.index, id=str(document_id))
            return response["_source"]
        except:
            return None

    def get_all(self, skip: int = 0, limit: int = 10):
        response = self.es.search(
            index=self.index,
            from_=skip,
            size=limit,
            query={"match_all": {}}
        )
        return [hit["_source"] for hit in response["hits"]["hits"]]

    def update(self, document_id, document):
        return self.es.update(index=self.index, id=str(document_id), doc=document, refresh=True)

    def delete(self, document_id):
        try:
            return self.es.delete(index=self.index, id=str(document_id), refresh=True)
        except:
            return None

    def count(self):
        return self.es.count(index=self.index)["count"]
