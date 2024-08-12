from typing import Dict, Any, Tuple
from utils.helper import get_data_from_firestore
from config.logger import logger

class DocVerifier:
    @staticmethod
    def verify_patient_name(data: Dict[str, Any], user_id: str) -> Tuple[bool, str]:
        patient_data = get_data_from_firestore(f'patients/{user_id}')
        if not patient_data or not patient_data.get('kyc_data') or not patient_data['kyc_data'].get('aadhar_data'):
            logger.error(f"Failed to retrieve patient data for user {user_id}")
            return False, "Failed to verify patient name: No patient data found"

        doc_name = data['patient_name'].strip().lower()
        db_name = patient_data['kyc_data']['aadhar_data'].get('name', '').strip().lower()

        if doc_name != db_name:
            logger.error(f"Failed to verify patient name for user {user_id}. "
                         f"Patient name: '{doc_name}' (len: {len(doc_name)}), "
                         f"Patient name in DB: '{db_name}' (len: {len(db_name)})")
            return False, "Failed to verify patient name"
        return True, "Patient name verified successfully"
    
    @staticmethod
    def verify_doctor_name(data: Dict[str, Any], user_id: str) -> Tuple[bool, str]:
        doctor_data = get_data_from_firestore(f'patients/{user_id}')
        if not doctor_data:
            logger.error(f"Failed to retrieve patient data for user {user_id}")
            return False, "Failed to verify doctor name: No patient data found"

        doc_name = data['doctor_name'].strip().lower()
        db_name = doctor_data.get('primary_doctor_name', '').strip().lower()

        if doc_name != db_name:
            logger.error(f"Failed to verify doctor name for user {user_id}. "
                         f"Doctor name: '{doc_name}' (len: {len(doc_name)}), "
                         f"Doctor name in DB: '{db_name}' (len: {len(db_name)})")
            return False, "Failed to verify doctor name"
        return True, "Doctor name verified successfully"
    
    @staticmethod
    def verify_hospital_name(data: Dict[str, Any], user_id: str) -> Tuple[bool, str]:
        doctor_data = get_data_from_firestore(f'patients/{user_id}')
        if not doctor_data:
            logger.error(f"Failed to retrieve patient data for user {user_id}")
            return False, "Failed to verify hospital name: No patient data found"

        doc_name = data['hospital_name'].strip().lower()
        db_name = doctor_data.get('primary_healthcare_provider', '').strip().lower()

        if doc_name != db_name:
            logger.error(f"Failed to verify hospital name for user {user_id}. "
                         f"Hospital name: '{doc_name}' (len: {len(doc_name)}), "
                         f"Hospital name in DB: '{db_name}' (len: {len(db_name)})")
            return False, "Failed to verify hospital name"
        return True, "Hospital name verified successfully"

    @staticmethod
    def verify_discharge_bill_data(data: Dict[str, Any], user_id: str) -> Tuple[str, bool]:
        is_patient_verified, message = DocVerifier.verify_patient_name(data, user_id)
        if not is_patient_verified:
            return message, False

        # is_doctor_verified, message = DocVerifier.verify_doctor_name(data, user_id)
        # if not is_doctor_verified:
        #     return message, False
        
        # is_hospital_verified, message = DocVerifier.verify_hospital_name(data, user_id)
        # if not is_hospital_verified:
        #     return message, False
        
        return "Discharge bill verified successfully", True

    @staticmethod
    def verify_pharmacy_bill_data(data: Dict[str, Any], user_id: str) -> Tuple[str, bool]:
        name_verified, message = DocVerifier.verify_patient_name(data, user_id)
        if not name_verified:
            return message, False
        
        return "Pharmacy bill verified successfully", True