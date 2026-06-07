from datetime import datetime
from pydantic import BaseModel

class ReportBase(BaseModel):
    id: int
    description: str
    createdAt: datetime
    resolvedAt: datetime
    reportStatus: str
    offerId: int
    plantId: int
    plantName: str
    speciesName: str
    plantTypeName: str
    feedbackRating: int
    feedbackComment: str

class ReportCreate(ReportBase):
    pass

class Report(ReportBase):
    pass

class UserBase(BaseModel):
    id: int
    username: str
    firstName: str
    lastName: str
    email: str

class UserCreate(UserBase):
    pass

class User(UserBase):
    phoneNumber: str
    address: str
    city: str
    country: str
    zipCode: str
    totalPurchases: int
    totalReports: int
    createdAt: datetime
    reports: list[Report]
