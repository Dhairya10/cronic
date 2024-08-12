from pydantic import BaseModel, Field
from typing import Optional
from enum import Enum
from datetime import date

class Gender(str, Enum):
    MALE = "male"
    FEMALE = "female"
    OTHER = "other"

class Address(BaseModel):
    street: str
    city: str
    state: str
    pincode: str

class AadharFrontResponse(BaseModel):
    aadhar_number: str = Field(..., description="12-digit Aadhar number")
    name: str
    date_of_birth: date
    gender: Gender

    class Config:
        schema_extra = {
            "example": {
                "aadhar_number": "123456789012",
                "name": "John Doe",
                "date_of_birth": "1990-01-01",
                "gender": "male"
            }
        }

class AadharBackResponse(BaseModel):
    address: Address

    class Config:
        schema_extra = {
            "example": {
                "address": {
                    "street": "123 Main St",
                    "city": "Mumbai",
                    "state": "Maharashtra",
                    "pincode": "400001"
                }
            }
        }

class PANData(BaseModel):
    pan_number: str = Field(..., description="10-character PAN number")
    name: str

class BankAccountData(BaseModel):
    account_number: str
    ifsc_code: str
    bank_name: str
    branch_name: str
    account_holder_name: str

class KYCDocumentResponse(BaseModel):
    aadhar_front_data: Optional[AadharFrontResponse] = Field(None, description="Data extracted from Aadhar card front")
    aadhar_back_data: Optional[AadharBackResponse] = Field(None, description="Data extracted from Aadhar card back")
    pan_data: Optional[PANData] = Field(None, description="Data extracted from PAN card")
    bank_account_data: Optional[BankAccountData] = Field(None, description="Data extracted from bank document")
    
    class Config:
        schema_extra = {
            "example": {
                "aadhar_front_data": {
                    "aadhar_number": "123456789012",
                    "name": "John Doe",
                    "date_of_birth": "1990-01-01",
                    "gender": "male"
                },
                "aadhar_back_data": {
                    "address": {
                        "street": "123 Main St",
                        "city": "Mumbai",
                        "state": "Maharashtra",
                        "pincode": "400001"
                    }
                },
                "pan_data": {
                    "pan_number": "ABCDE1234F",
                    "name": "John Doe"
                },
                "bank_account_data": {
                    "account_number": "1234567890",
                    "ifsc_code": "ABCD0123456",
                    "bank_name": "State Bank of India",
                    "branch_name": "Mumbai Main Branch",
                    "account_holder_name": "John Doe"
                }
            }
        }