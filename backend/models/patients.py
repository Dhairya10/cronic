import re
from pydantic import BaseModel, Field, field_validator
from typing import Optional
from datetime import date
from enum import Enum

class Gender(str, Enum):
    MALE = "male"
    FEMALE = "female"
    OTHER = "other"

class HealthCondition(str, Enum):
    CHRONIC_KIDNEY_DISEASE = "chronic_kidney_disease"
    THALASSEMIA = "thalassemia"

class IncomeLevel(str, Enum):
    LESS_THAN_2 = "less_than_2"
    TWO_TO_FIVE = "2_to_5"
    FIVE_TO_TEN = "5_to_10"
    TEN_PLUS = "10_plus"

class Address(BaseModel):
    street: str
    city: str
    state: str
    pincode: str

class AadharData(BaseModel):
    aadhar_number: str = Field(..., description="12-digit Aadhar number")
    name: str
    date_of_birth: date
    gender: Gender
    address: Address

class PANData(BaseModel):
    pan_number: str = Field(..., description="10-character PAN number")
    name: str

class BankAccountData(BaseModel):
    account_number: str
    ifsc_code: str
    bank_name: str
    branch_name: str
    account_holder_name: str

class KYCData(BaseModel):
    aadhar_data: Optional[AadharData] = None
    pan_data: Optional[PANData] = None
    bank_account_data: Optional[BankAccountData] = None
    income_level: Optional[IncomeLevel] = None

class PatientCreate(BaseModel):
    uhid: Optional[str] = None
    contact_num: str
    health_condition: HealthCondition
    primary_doctor_name: str
    primary_healthcare_provider: str
    kyc_data: Optional[KYCData] = None

    @field_validator('contact_num')
    @classmethod
    def validate_phone_number(cls, v):
        if not re.match(r'^\+?1?\d{9,15}$', v):
            raise ValueError('Invalid phone number')
        return v

    class Config:
        json_schema_extra = {
            "example": {
                "uhid": "UHID123456",
                "contact_num": "+911234567890",
                "health_condition": "chronic_kidney_disease",
                "primary_doctor_name": "Dr. Jane Smith",
                "primary_healthcare_provider": "City Hospital",
                "kyc_data": {
                    "aadhar_data": {
                        "aadhar_number": "123456789012",
                        "name": "John Doe",
                        "date_of_birth": "1990-01-01",
                        "gender": "male",
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
                    },
                    "income_level": "two_to_five"
                }
            }
        }

class Patient(BaseModel):
    id: str
    uhid: Optional[str] = None
    contact_num: str
    health_condition: HealthCondition
    primary_doctor_name: str
    primary_healthcare_provider: str
    kyc_data: Optional[KYCData] = None

    class Config:
        arbitrary_types_allowed = True
        json_encoders = {
            date: lambda v: v.isoformat()
        }