import asyncio
from fastapi import APIRouter, Depends, HTTPException
from fastapi.security import HTTPBearer
from models.documents import DocumentInput, DocumentResponse, BatchDocumentInput
from typing import Dict
from services.doc_service import DocService
from auth.verify import verify_token
from config.settings import initialize_model
from config.logger import logger
from config.settings import process_pool, file_processing_semaphore, settings

router = APIRouter()
security = HTTPBearer()

def get_file_service():
    model = initialize_model()
    return DocService(model)

@router.post("/verify", response_model=DocumentResponse)
async def verify_claim(
    document: DocumentInput,
    token: Dict = Depends(verify_token),
    file_service: DocService = Depends(get_file_service)
):
    user_id = token.get("uid")
    
    try:
        async with file_processing_semaphore:
            try:
                async with asyncio.timeout(settings.PROCESSING_TIMEOUT):
                    status = await asyncio.get_event_loop().run_in_executor(
                        process_pool,
                        file_service.get_claim_status,
                        user_id,
                        str(document.file_uri),
                        document.document_type
                    )
            except asyncio.TimeoutError:
                logger.error(f"Document processing timed out for user {user_id}")
                raise HTTPException(status_code=504, detail="Document processing timed out")

        if status:
            logger.info(f"Successfully processed document for user {user_id}")

        return DocumentResponse(status=status)

    except Exception as e:
        logger.error(f"Error processing document: {str(e)}")
        raise HTTPException(status_code=500, detail="An error occurred while processing the document")


@router.post("/verify-batch", response_model=DocumentResponse)
async def verify_claim_batch(
    documents: BatchDocumentInput,
    token: Dict = Depends(verify_token),
    file_service: DocService = Depends(get_file_service)
):
    user_id = token.get("uid")
    
    async def process_document(document: DocumentInput) -> bool:
        try:
            async with file_processing_semaphore:
                try:
                    async with asyncio.timeout(settings.PROCESSING_TIMEOUT):
                        status = await asyncio.get_event_loop().run_in_executor(
                            process_pool,
                            file_service.get_claim_status,
                            user_id,
                            str(document.file_uri),
                            document.document_type
                        )
                except asyncio.TimeoutError:
                    logger.error(f"Document processing timed out for user {user_id}")
                    return False
                if status:
                    logger.info(f"Successfully processed document {document.file_uri} for user {user_id}")
                return status
        except Exception as e:
            logger.error(f"Error processing document {document.file_uri}: {str(e)}")
            return False

    try:
        tasks = [process_document(document) for document in documents.documents]
        statuses = await asyncio.gather(*tasks)
        # Check if all documents were processed successfully
        all_successful = all(statuses)

        return DocumentResponse(status=all_successful)

    except Exception as e:
        logger.error(f"Error processing batch of documents: {str(e)}")
        raise HTTPException(status_code=500, detail="An error occurred while processing the documents")