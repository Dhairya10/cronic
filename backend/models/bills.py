from pydantic import BaseModel, Field
from datetime import datetime
from enum import Enum
from typing import Optional, List

class BillType(str, Enum):
    PHARMACY = "pharmacy"
    DISCHARGE = "discharge"
    CONSULTATION = "consultation"

class BillStatus(str, Enum):
    VERIFIED = "verified"
    PENDING = "pending"
    REJECTED = "rejected"

class BillCreate(BaseModel):
    patient_id: str
    date: datetime
    amount: float
    status: BillStatus
    reasoning: Optional[str] = None
    type: BillType

class Bill(BaseModel):
    id: str
    patient_id: str
    date: datetime = Field(default_factory=datetime.utcnow)
    amount: float
    status: BillStatus = BillStatus.PENDING
    reasoning: Optional[str] = None
    type: BillType

    class Config:
        arbitrary_types_allowed = True
        json_encoders = {
            datetime: lambda v: v.isoformat()
        }

class UserBillsResponse(BaseModel):
    bills: List[Bill]