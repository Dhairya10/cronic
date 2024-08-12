import asyncio
from fastapi import APIRouter, Depends, HTTPException
from fastapi.security import HTTPBearer
from typing import Dict
from models.documents import DocumentInput
from models.kyc import KYCDocumentResponse
from services.doc_service import DocService
from auth.verify import verify_token
from config.logger import logger
from config.settings import process_pool, file_processing_semaphore, initialize_model, settings

router = APIRouter()
security = HTTPBearer()

def get_file_service():
    model = initialize_model()
    return DocService(model)

@router.post("/verify", response_model=KYCDocumentResponse)
async def verify_kyc_document(
    document: DocumentInput,
    token: Dict = Depends(verify_token),
    file_service: DocService = Depends(get_file_service)
):
    user_id = token.get("uid")
    
    try:
        async with file_processing_semaphore:
            try:
                async with asyncio.timeout(settings.PROCESSING_TIMEOUT):
                    extracted_data = await asyncio.get_event_loop().run_in_executor(
                        process_pool,
                        file_service.get_kyc_data,
                        str(document.file_uri),
                        document.document_type
                    )
            except asyncio.TimeoutError:
                logger.error(f"KYC document processing timed out for user {user_id}")
                raise HTTPException(status_code=504, detail="Document processing timed out")

        if extracted_data:
            logger.info(f"Successfully processed KYC document for user {user_id} with data : {extracted_data}")
            logger.info(f"Response : {KYCDocumentResponse(**extracted_data)}")
            return KYCDocumentResponse(**extracted_data)
        else:
            raise HTTPException(status_code=400, detail="Failed to extract data from the document")

    except Exception as e:
        logger.error(f"Error processing KYC document: {str(e)}")
        raise HTTPException(status_code=500, detail="An error occurred while processing the document")