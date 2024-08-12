
# Cronic - Backend

This project is a FastAPI-based API for processing and verifying various types of documents, including KYC (Know Your Customer) documents and medical claims. It uses Gemini for document analysis and Firebase for authentication and data storage.

## Features

- Document verification for KYC and medical claims
- Integration with Gemini for document analysis
- Firebase authentication and Firestore database integration
- Asynchronous processing with timeout handling
- Modular architecture with separate services for different functionalities

## Setup

1. Clone the repository
2. Install dependencies:
   ```
   pip install -r requirements.txt
   ```
3. Set up environment variables in a `.env` file:
   ```
   ENVIRONMENT=production
   MODEL_NAME=your_gemini_model_name
   GEMINI_API_KEY=your_gemini_api_key
   FIREBASE_CREDENTIALS_PATH=path_to_your_firebase_credentials.json
   FIREBASE_STORAGE_BUCKET=your_firebase_storage_bucket
   PROCESSING_TIMEOUT=30
   ```

## Project Structure

- `app.py`: Main FastAPI application
- `routers/`: API route definitions
- `services/`: Business logic for document processing and verification
- `models/`: Pydantic models for request/response schemas
- `auth/`: Authentication utilities
- `config/`: Configuration and settings
- `utils/`: Helper functions

## API Endpoints

- `/claim/verify`: Verify claim documents
- `/kyc/verify`: Verify KYC documents
- `/patient/`: Patient-related operations
- `/bills/`: Bill-related operations
- `/hospital/`: Hospital information

## Running the Application

To run the application, run the following command

```
python app.py
```