from pydantic import BaseModel, Field
from typing import List
from enum import Enum

class DocumentType(str, Enum):
    IMAGE = "image"
    PDF = "pdf"

class DocumentInput(BaseModel):
    file_uri: str = Field(..., description="Firebase Storage URI of the document")
    document_type: DocumentType = Field(..., description="Type of the document (image or pdf)")

class BatchDocumentInput(BaseModel):
    documents: List[DocumentInput] = Field(..., min_items=1, max_items=10, description="List of documents to process")

class DocumentResponse(BaseModel):
    status: bool = Field(..., description="True if document(s) were processed successfully, False otherwise")