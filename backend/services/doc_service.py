import google.generativeai as genai
from utils.helper import upload_to_gemini, download_from_storage, pdf_to_images
from typing import Dict, Any
from services.doc_processor import DocProcessor
from config.settings import settings
from models.documents import DocumentType
from config.logger import logger
from enum import Enum

class ProcessingType(Enum):
    CLAIM = 'claim'
    KYC = 'kyc'

class DocService:
    """
    A service class for processing various types of documents using the Gemini API.
    """

    def __init__(self, model: genai.GenerativeModel):
        """
        Initialize the DocService with a Gemini model.

        Args:
            model (genai.GenerativeModel): The Gemini model to use for processing.
        """
        self.model = model
        self.doc_processor = DocProcessor(model)


    def _process_single_image(self, user_id: str, image_path: str, processing_type: ProcessingType) -> Dict[str, Any]:
        """
        Process a single image file for either claim or KYC data extraction.

        Args:
            user_id (str): The user ID, required for claim processing.
            image_path (str): The path to the image file.
            processing_type (ProcessingType): The type of processing to perform (CLAIM or KYC).

        Returns:
            Dict[str, Any]: The extracted data from the image.
        """
        document = upload_to_gemini(image_path)
        if document is None:
            raise Exception(f"Failed to upload image {image_path} to Gemini. Document is None")
        if processing_type == ProcessingType.CLAIM:
            return self.doc_processor.process_claim(document, user_id)
        elif processing_type == ProcessingType.KYC:
            return self.doc_processor.process_kyc(document)
        else:
            raise ValueError(f"Invalid processing type: {processing_type}")


    def get_claim_status(self, user_id: str, file_uri: str, document_type: DocumentType) -> bool:
        """
        Process a document from a given URI.

        Args:
            user_id (str): The ID of the user.
            file_uri (str): The URI of the file.
            document_type (DocumentType): The type of the document (image or pdf).

        Returns:
            bool: True if processing was successful, False otherwise.

        Raises:
            Exception: If there's an error during file download or processing.
        """
        try:
            temp_file_path = download_from_storage(file_uri, settings.FIREBASE_STORAGE_BUCKET)
            
            if document_type == DocumentType.PDF:
                path_list = pdf_to_images(temp_file_path)
                results = []
                for image_path in path_list:
                    result = self._process_single_image(user_id, image_path, ProcessingType.CLAIM)
                    results.append(result)
                    logger.info(f"Processed image: {image_path} with status: {result}")
                return all(results)
            elif document_type == DocumentType.IMAGE:
                return self._process_single_image(user_id, temp_file_path, ProcessingType.CLAIM)
            else:
                logger.error(f"Unsupported document type: {document_type}")
                return False
        except Exception as e:
            logger.error(f"Error processing file from URI {file_uri}: {str(e)}")
            return False


    def get_kyc_data(self, file_uri: str, document_type: DocumentType) -> Dict[str, Any]:
        """
        Process a KYC document from a given URI.

        Args:
            file_uri (str): The URI of the file.
            document_type (DocumentType): The type of the document (image or pdf).

        Returns:
            Dict[str, Any]: The extracted data from the document.
        """
        try:
            temp_file_path = download_from_storage(file_uri, settings.FIREBASE_STORAGE_BUCKET)
            
            if document_type == DocumentType.PDF:
                image_paths = pdf_to_images(temp_file_path)
                extracted_data = {}
                for image_path in image_paths:
                    page_data = self._process_single_image(None, image_path, ProcessingType.KYC)
                    extracted_data.update(page_data or {})
            else:
                extracted_data = self._process_single_image(None, temp_file_path, ProcessingType.KYC)
            
            logger.info(f"Extracted data : {extracted_data}")
            return extracted_data
        except Exception as e:
            logger.error(f"Error processing KYC file: {str(e)}")
            return {}