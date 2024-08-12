from fastapi import APIRouter, HTTPException, Depends
from typing import List, Dict
from models.hospital import Hospital
from auth.verify import verify_token
from utils.helper import get_data_from_firestore
from config.logger import logger

router = APIRouter()

@router.get("/", response_model=List[Hospital])
async def get_hospitals(token: Dict = Depends(verify_token)):
    try:
        hospitals_data = get_data_from_firestore('hospitals')
        
        if hospitals_data is None:
            raise HTTPException(status_code=404, detail="No hospitals found")
        
        hospitals = [Hospital(**hospital) for hospital in hospitals_data]
        
        logger.info(f"Successfully retrieved {len(hospitals)} hospitals")
        return hospitals
    except Exception as e:
        logger.error(f"Error retrieving hospitals: {str(e)}")
        raise HTTPException(status_code=500, detail=f"An error occurred: {str(e)}")

@router.get("/{hospital_id}", response_model=Hospital)
async def get_hospital(hospital_id: str, token: Dict = Depends(verify_token)):
    try:
        hospital_data = get_data_from_firestore(f'hospitals/{hospital_id}')
        
        if hospital_data is None:
            raise HTTPException(status_code=404, detail="Hospital not found")
        
        logger.info(f"Successfully retrieved hospital with ID: {hospital_id}")
        return Hospital(**hospital_data)
    except Exception as e:
        logger.error(f"Error retrieving hospital with ID {hospital_id}: {str(e)}")
        raise HTTPException(status_code=500, detail=f"An error occurred: {str(e)}")