from fastapi import APIRouter, Depends, HTTPException
from typing import Dict
from models.bills import Bill, UserBillsResponse
from auth.verify import verify_token
from utils.helper import get_data_from_firestore
from config.logger import logger

router = APIRouter()

@router.get("/", response_model=UserBillsResponse)
async def get_user_bills(token: Dict = Depends(verify_token)):
    try:
        user_id = token.get("uid")
        bills_data = get_data_from_firestore('bills', filter_field='patient_id', filter_value=user_id)
        
        if bills_data is None:
            raise HTTPException(status_code=404, detail="No bills found")
        
        user_bills = [Bill(**bill) for bill in bills_data]
        
        logger.info(f"Successfully retrieved {len(user_bills)} bills for user {user_id}")
        return UserBillsResponse(bills=user_bills)
    except Exception as e:
        logger.error(f"Error retrieving bills for user {user_id}: {str(e)}")
        raise HTTPException(status_code=500, detail=f"An error occurred: {str(e)}")