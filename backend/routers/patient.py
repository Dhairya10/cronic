from fastapi import APIRouter, Depends, HTTPException
from fastapi.security import HTTPBearer
from typing import Dict, Any
from models.patients import PatientCreate, Patient
from services.patient_service import PatientService
from auth.verify import verify_token
from config.logger import logger
from utils.helper import get_data_from_firestore

router = APIRouter()
security = HTTPBearer()

def get_patient_service():
    return PatientService()

@router.post("/add", response_model=Dict[str, Any])
async def add_patient(
    patient_data: PatientCreate,
    token: Dict = Depends(verify_token),
    patient_service: PatientService = Depends(get_patient_service)
):
    try:
        user_id = token.get("uid")
        # Validate the patient data
        validation_result = patient_service.validate_patient_data(patient_data)
        if not validation_result['is_valid']:
            raise HTTPException(status_code=400, detail=validation_result['errors'])

        # Add patient to Firestore
        patient_id = patient_service.add_patient(patient_data, user_id)

        return {"status": "success", "patient_id": patient_id}
    except HTTPException as he:
        raise he
    except Exception as e:
        logger.error(f"Error adding patient: {str(e)}")
        raise HTTPException(status_code=500, detail="An error occurred while adding the patient")


@router.get("/", response_model=Dict[str, Any])
async def get_patient(token: Dict = Depends(verify_token)):
    try:
        user_id = token.get("uid")
        patients_data = get_data_from_firestore('patients', filter_field='id', filter_value=user_id)
        
        if not patients_data:
            logger.warning(f"No patient found with id {user_id}")
            return {"patient_data": None}
        
        if isinstance(patients_data, list):
            if len(patients_data) > 1:
                logger.warning(f"Multiple patients found with id {user_id}")
            patient_data = patients_data[0]
        elif isinstance(patients_data, dict):
            patient_data = patients_data
        else:
            raise ValueError(f"Unexpected data type returned from Firestore: {type(patients_data)}")
        
        return {"patient_data": Patient(**patient_data)}
    except Exception as e:
        logger.error(f"Error retrieving patient data: {str(e)}")
        raise HTTPException(status_code=500, detail="An error occurred while retrieving patient data")