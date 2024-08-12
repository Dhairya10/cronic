import os
import re
import json
import tempfile
import google.generativeai as genai
from typing import List, Dict, Any, Union
from firebase_admin import firestore
from config.logger import logger
from firebase_admin import storage
from pdf2image import convert_from_path

def get_firestore_client():
    return firestore.client()

#### General helper functions

def load_prompt_from_file(file_path, key):
    with open(file_path, 'r') as file:
        data = json.load(file)
    return data[key]

def convert_json_string_to_dict(json_string):
    if not json_string:
        raise ValueError("JSON string is empty")
    
    # Remove 'json' prefix if it exists
    # if json_string.startswith('json'):
    #     json_string = json_string[4:]
    json_string = re.sub(r'^```json\n|\n```$', '', json_string.strip())
    
    logger.info(f"Attempting to parse JSON string: {json_string}")
    
    try:
        json_string = json_string.strip()
        return json.loads(json_string)
    except json.JSONDecodeError as e:
        logger.info(f"JSON Decode Error: {e}")
        logger.info(f"JSON string causing the error: {json_string}")
        raise


def pdf_to_images(pdf_path, output_dir=None, dpi=200, format='png'):
    """
    Convert a PDF file to a list of images.
    
    :param pdf_path: Path to the PDF file
    :param output_dir: Directory to save the images (default is a temporary directory)
    :param dpi: DPI for the output images (default is 200)
    :param format: Format of the output images (default is 'png')
    :return: List of paths to the saved images
    """
    # Create a temporary directory if output_dir is not provided
    if output_dir is None:
        output_dir = tempfile.mkdtemp()
    else:
        os.makedirs(output_dir, exist_ok=True)

    # Convert PDF to list of images
    images = convert_from_path(pdf_path, dpi=dpi)

    # Save each image
    image_paths = []
    for i, image in enumerate(images):
        file_name = f'page_{i+1}.{format}'
        file_path = os.path.join(output_dir, file_name)
        image.save(file_path, format)
        image_paths.append(file_path)

    return image_paths


#### Firebase helper functions

def download_from_storage(firebase_uri, bucket_name, output_dir="output"):
    # Ensure the output directory exists
    os.makedirs(output_dir, exist_ok=True)

    # Initialize Firebase Storage bucket
    bucket = storage.bucket(bucket_name)

    # Extract the file path from the Firebase Storage URI
    file_path = firebase_uri.split('/', 3)[-1]
    
    # Create a blob object
    blob = bucket.blob(file_path)

    # Create a temporary file
    with tempfile.NamedTemporaryFile(delete=False, dir=output_dir) as temp_file:
        temp_file_path = temp_file.name

    # Download the file to the temporary location
    blob.download_to_filename(temp_file_path)
    logger.info(f"Downloaded file to: {temp_file_path}")

    return temp_file_path


def add_data_to_firestore(data_dict: Dict, collection_path: str, document_id: str = None) -> Union[str, None]:
    """
    Add data to Firestore in a specified collection or subcollection.

    Args:
    data_dict (Dict): The data to be added to Firestore.
    collection_path (str): The path to the collection or subcollection, using '/' as separator.
                           E.g., "users" or "users/user123/orders"
    document_id (str, optional): The document ID. If None, Firestore will auto-generate an ID.

    Returns:
    Union[str, None]: The ID of the added document, or None if an error occurred.
    """
    db = get_firestore_client()
    
    try:
        # Split the path and navigate to the specified collection or subcollection
        path_parts = collection_path.split('/')
        collection_ref = db
        for i, part in enumerate(path_parts):
            if i % 2 == 0:
                collection_ref = collection_ref.collection(part)
            else:
                collection_ref = collection_ref.document(part)
                
        # Add server timestamp to the data
        data_dict['created_at'] = firestore.SERVER_TIMESTAMP
        data_dict['updated_at'] = firestore.SERVER_TIMESTAMP

        # Add the data to Firestore
        if document_id:
            doc_ref = collection_ref.document(document_id)
            doc_ref.set(data_dict)
        else:
            doc_ref = collection_ref.add(data_dict)[1]

        # Add the document ID to the data
        data_dict['id'] = doc_ref.id
        doc_ref.update({'id': doc_ref.id})

        logger.info(f"Document added with ID: {doc_ref.id} in collection: {collection_path}")
        return doc_ref.id

    except Exception as e:
        logger.error(f"An error occurred while adding data to Firestore: {str(e)}")
        return None


def get_data_from_firestore(
    path: str, 
    filter_field: str = None, 
    filter_value: Any = None, 
    document_id: str = None, 
    include_subcollections: bool = True
) -> Union[Dict, List[Dict], None]:
    db = get_firestore_client()
    
    try:
        # Split the path and navigate to the specified location
        path_parts = path.split('/')
        current_ref = db
        for i, part in enumerate(path_parts):
            if i % 2 == 0:
                current_ref = current_ref.collection(part)
            else:
                current_ref = current_ref.document(part)

        # Fetch the data
        if isinstance(current_ref, firestore.DocumentReference):
            # Processing a document
            doc = current_ref.get()
            if doc.exists:
                logger.info(f"Document fetched from {path}")
                data = doc.to_dict()
                data['id'] = doc.id
                if include_subcollections:
                    subcollections = current_ref.collections()
                    for subcoll in subcollections:
                        data[subcoll.id] = get_data_from_firestore(f"{path}/{subcoll.id}")
                return data
            else:
                logger.info(f"Document not found at {path}")
                return None
        elif isinstance(current_ref, firestore.CollectionReference):
            # Processing a collection
            if document_id:
                return get_data_from_firestore(f"{path}/{document_id}", include_subcollections=include_subcollections)
            else:
                # Apply filter if provided
                if filter_field and filter_value:
                    query = current_ref.where(filter_field, '==', filter_value)
                else:
                    query = current_ref

                docs = query.stream()
                result = []
                for doc in docs:
                    data = doc.to_dict()
                    data['id'] = doc.id
                    if include_subcollections:
                        subcollections = doc.reference.collections()
                        for subcoll in subcollections:
                            data[subcoll.id] = get_data_from_firestore(f"{path}/{doc.id}/{subcoll.id}")
                    result.append(data)
                logger.info(f"Fetched {len(result)} documents from {path}")
                return result

    except Exception as e:
        logger.error(f"An error occurred while fetching data from Firestore: {str(e)}")
        return None

#### Gemini helper functions

def upload_to_gemini(file_path, remove_after_upload=True):
    try:
        # Debug information
        logger.info(f"Attempting to upload file: {file_path}")
        logger.info(f"File exists: {os.path.exists(file_path)}")
        logger.info(f"File size: {os.path.getsize(file_path)} bytes")

        # Explicitly set MIME type to a supported image format
        mime_type = 'image/png'  # or 'image/jpeg' if you're sure it's a JPEG

        # Upload the file with the explicit MIME type
        patient_doc = genai.upload_file(path=file_path,
                                        display_name="patient_doc",
                                        mime_type=mime_type)
        logger.info(f"Uploaded file '{patient_doc.display_name}' as: {patient_doc.uri}")
        
        # Attempt to remove the file from the local directory
        if remove_after_upload:
            try:
                os.remove(file_path)
                logger.info(f"Successfully removed local file: {file_path}")
            except OSError as e:
                logger.warning(f"Failed to remove local file {file_path}: {e}")
        
        return patient_doc
    except Exception as e:
        logger.error(f"Error uploading file to Gemini: {e}")
        logger.error(f"File path: {file_path}")
        logger.error(f"File exists: {os.path.exists(file_path)}")
        if os.path.exists(file_path):
            logger.error(f"File size: {os.path.getsize(file_path)} bytes")
        return None