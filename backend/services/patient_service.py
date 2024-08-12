from models.patients import PatientCreate
from typing import Dict
import re
from datetime import date
from utils.helper import add_data_to_firestore
from config.logger import logger

class PatientService:
    def add_patient(self, patient_data: PatientCreate, user_id: str) -> str:
        """
        Add a new patient to Firestore.

        Args:
        patient_data (PatientCreate): The patient data to be added.
        user_id (str): The user ID from the token.

        Returns:
        str: The ID of the added patient document.

        Raises:
        Exception: If failed to add patient to Firestore.
        """
        collection_path = "patients"
        patient_dict = patient_data.model_dump()
        
        # Convert date of birth to ISO format string
        if patient_dict.get('kyc_data') and patient_dict['kyc_data'].get('aadhar_data'):
            dob = patient_dict['kyc_data']['aadhar_data'].get('date_of_birth')
            if isinstance(dob, date):
                patient_dict['kyc_data']['aadhar_data']['date_of_birth'] = dob.isoformat()
        
        # Set the id to the user_id from the token
        patient_dict['id'] = user_id
        
        patient_id = add_data_to_firestore(patient_dict, collection_path, document_id=user_id)
        
        if patient_id:
            logger.info(f"Patient added with ID: {patient_id}")
            return patient_id
        else:
            logger.error("Failed to add patient to Firestore")
            raise Exception("Failed to add patient to Firestore")


    def validate_patient_data(self, patient: PatientCreate) -> Dict[str, any]:
        errors = []

        # Validate DOB
        today = date.today()
        dob = patient.kyc_data.aadhar_data.date_of_birth
        if dob > today:
            errors.append("Date of birth cannot be in the future")
        else:
            age = today.year - dob.year - ((today.month, today.day) < (dob.month, dob.day))
            if age < 1 or age > 100:
                errors.append("Patient's age must be between 1 and 100 years")

        # Validate Aadhar number if provided
        if patient.kyc_data and patient.kyc_data.aadhar_data:
            if not re.match(r'^\d{12}$', patient.kyc_data.aadhar_data.aadhar_number):
                errors.append("Invalid Aadhar number")

        # Validate PAN number if provided
        if patient.kyc_data and patient.kyc_data.pan_data:
            if not re.match(r'^[A-Z]{5}[0-9]{4}[A-Z]$', patient.kyc_data.pan_data.pan_number):
                errors.append("Invalid PAN number")

        # Validate bank account number if provided
        if patient.kyc_data and patient.kyc_data.bank_account_data:
            if not re.match(r'^\d{9,18}$', patient.kyc_data.bank_account_data.account_number):
                errors.append("Invalid bank account number")

        # Validate IFSC code if provided
        if patient.kyc_data and patient.kyc_data.bank_account_data:
            if not re.match(r'^[A-Z]{4}0[A-Z0-9]{6}$', patient.kyc_data.bank_account_data.ifsc_code):
                errors.append("Invalid IFSC code")

        return {
            "is_valid": len(errors) == 0,
            "errors": errors
        }