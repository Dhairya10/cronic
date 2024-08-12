from typing import Dict, Any, Tuple
import google.generativeai as genai
from utils.helper import load_prompt_from_file, add_data_to_firestore
from config.logger import logger
from datetime import datetime, date
from models.bills import BillCreate, BillType, BillStatus
from services.doc_verifier import DocVerifier
from proto.marshal.collections.maps import MapComposite
import re


class DocProcessor:
    def __init__(self, model: genai.GenerativeModel):
        self.model = model
        self.setup_function_declarations()

    def setup_function_declarations(self):

        # Claim documents
        discharge_bill_schema = genai.protos.Schema(
            type=genai.protos.Type.OBJECT,
            properties={
                'patient_name': genai.protos.Schema(type=genai.protos.Type.STRING),
                'doctor_name': genai.protos.Schema(type=genai.protos.Type.STRING),
                'total_amount': genai.protos.Schema(type=genai.protos.Type.NUMBER),
                'date': genai.protos.Schema(type=genai.protos.Type.STRING),
                'hospital_name': genai.protos.Schema(type=genai.protos.Type.STRING)
            },
            required=['patient_name', 'doctor_name', 'total_amount', 'date', 'hospital_name']
        )

        pharmacy_bill_schema = genai.protos.Schema(
            type=genai.protos.Type.OBJECT,
            properties={
                'patient_name': genai.protos.Schema(type=genai.protos.Type.STRING),
                'total_amount': genai.protos.Schema(type=genai.protos.Type.NUMBER),
                'date': genai.protos.Schema(type=genai.protos.Type.STRING)
            },
            required=['patient_name', 'total_amount', 'date']
        )

        # KYC documents
        prescription_schema = genai.protos.Schema(
            type=genai.protos.Type.OBJECT,
            properties={
                'patient_name': genai.protos.Schema(type=genai.protos.Type.STRING),
                'doctor_name': genai.protos.Schema(type=genai.protos.Type.STRING),
                'hospital_name': genai.protos.Schema(type=genai.protos.Type.STRING)
            },
            required=['patient_name', 'doctor_name', 'hospital_name']
        )

        aadhar_card_front_schema = genai.protos.Schema(
            type=genai.protos.Type.OBJECT,
            properties={
                'name': genai.protos.Schema(type=genai.protos.Type.STRING),
                'aadhar_number': genai.protos.Schema(type=genai.protos.Type.STRING),
                'date_of_birth': genai.protos.Schema(type=genai.protos.Type.STRING),
                'gender': genai.protos.Schema(type=genai.protos.Type.STRING)
            },
            required=['name', 'aadhar_number', 'date_of_birth', 'gender']
        )

        aadhar_card_back_schema = genai.protos.Schema(
            type=genai.protos.Type.OBJECT,
            properties={
                'street': genai.protos.Schema(type=genai.protos.Type.STRING),
                'city': genai.protos.Schema(type=genai.protos.Type.STRING),
                'state': genai.protos.Schema(type=genai.protos.Type.STRING),
                'pincode': genai.protos.Schema(type=genai.protos.Type.STRING)
            },
            required=['street', 'city', 'state', 'pincode']
        )

        pan_card_schema = genai.protos.Schema(
            type=genai.protos.Type.OBJECT,
            properties={
                'name': genai.protos.Schema(type=genai.protos.Type.STRING),
                'pan_number': genai.protos.Schema(type=genai.protos.Type.STRING)
            },
            required=['name', 'pan_number']
        )

        bank_account_schema = genai.protos.Schema(
            type=genai.protos.Type.OBJECT,
            properties={
                'account_number': genai.protos.Schema(type=genai.protos.Type.STRING),
                'ifsc_code': genai.protos.Schema(type=genai.protos.Type.STRING),
                'bank_name': genai.protos.Schema(type=genai.protos.Type.STRING),
                'branch_name': genai.protos.Schema(type=genai.protos.Type.STRING),
                'account_holder_name': genai.protos.Schema(type=genai.protos.Type.STRING)
            },
            required=['account_number', 'ifsc_code', 'bank_name', 'branch_name', 'account_holder_name']
        )

        unrecognized_document_schema = genai.protos.Schema(
            type=genai.protos.Type.OBJECT,
            properties={
                'content': genai.protos.Schema(type=genai.protos.Type.STRING)
            },
            required=['content']
        )

        # Define function declarations for claim documents
        self.claim_function_declarations = [
            genai.protos.FunctionDeclaration(
                name="process_discharge_bill",
                description="Process a hospital discharge bill",
                parameters=discharge_bill_schema
            ),
            genai.protos.FunctionDeclaration(
                name="process_pharmacy_bill",
                description="Process a pharmacy bill",
                parameters=pharmacy_bill_schema
            )          
        ]
    
        # Define function declarations for KYC documents
        self.kyc_function_declarations = [
            genai.protos.FunctionDeclaration(
                name="process_prescription",
                description="Process a doctor's prescription",
                parameters=prescription_schema
            ),
            genai.protos.FunctionDeclaration(
                name="process_aadhar_card_front",
                description="Process the front side of an Aadhar card",
                parameters=aadhar_card_front_schema
            ),
            genai.protos.FunctionDeclaration(
                name="process_aadhar_card_back",
                description="Process the back side of an Aadhar card",
                parameters=aadhar_card_back_schema
            ),
            genai.protos.FunctionDeclaration(
                name="process_pan_card",
                description="Process a PAN card",
                parameters=pan_card_schema
            ),
            genai.protos.FunctionDeclaration(
                name="process_bank_account",
                description="Process bank account details",
                parameters=bank_account_schema
            ),
            genai.protos.FunctionDeclaration(
                name="process_unrecognized_document",
                description="Process an unrecognized document type",
                parameters=unrecognized_document_schema
            )
        ]


    def extract_function_call(self, response):
        if not response.candidates or len(response.candidates) == 0:
            logger.error("No candidates in the response")
            return None, None

        candidate = response.candidates[0]
        
        if candidate.finish_reason == "SAFETY":
            logger.warning("Response was blocked due to safety concerns")
            return None, None

        if not hasattr(candidate, 'content') or not hasattr(candidate.content, 'parts'):
            logger.warning("Invalid response structure")
            return None, None

        function_call = None
        function_name = None
        extracted_data = None

        for part in candidate.content.parts:
            if hasattr(part, 'function_call'):
                function_call = part.function_call
                break

        if function_call:
            function_name = function_call.name
            extracted_data = dict(function_call.args)
        else:
            # If no function call is found, check if there's text and a subsequent function call
            if len(candidate.content.parts) >= 2:
                if hasattr(candidate.content.parts[0], 'text') and hasattr(candidate.content.parts[1], 'function_call'):
                    function_call = candidate.content.parts[1].function_call
                    function_name = function_call.name
                    extracted_data = dict(function_call.args)

        if not function_name or not extracted_data:
            logger.warning("No function call found in the response")
            return None, None

        logger.info(f"Extracted function name: {function_name}")
        logger.info(f"Extracted data: {extracted_data}")

        return function_name, extracted_data


    def process_claim(self, document, user_id) -> bool:
        try:
            classification_prompt = load_prompt_from_file('config/prompt.json', 'prompt_claim_classification')

            safety_settings = [
                {
                    "category": "HARM_CATEGORY_DANGEROUS_CONTENT",
                    "threshold": "BLOCK_ONLY_HIGH"
                },
                {
                    "category": "HARM_CATEGORY_HARASSMENT",
                    "threshold": "BLOCK_ONLY_HIGH"
                },
                {
                    "category": "HARM_CATEGORY_HATE_SPEECH",
                    "threshold": "BLOCK_ONLY_HIGH"
                },
                {
                    "category": "HARM_CATEGORY_SEXUALLY_EXPLICIT",
                    "threshold": "BLOCK_ONLY_HIGH"
                },
            ]

            response = self.model.generate_content(
                [document, classification_prompt],
                safety_settings=safety_settings,
                generation_config={"temperature": 0.1},
                tools=[genai.protos.Tool(function_declarations=self.claim_function_declarations)]
            )
            logger.info(f"Function Calling Response: {response}")

            function_name, extracted_data = self.extract_function_call(response)

            if not function_name or not extracted_data:
                return False

            if isinstance(extracted_data, MapComposite):
                extracted_data = dict(extracted_data)

            if function_name not in ["process_discharge_bill", "process_pharmacy_bill"]:
                logger.error(f"Invalid function name: {function_name}")
                return False

            is_valid, validated_data = self.validate_extracted_data(function_name, extracted_data)
            if not is_valid:
                logger.error(f"Invalid data extracted for {function_name}: {extracted_data}")
                return False

            processors = {
                "process_discharge_bill": self.process_discharge_bill,
                "process_pharmacy_bill": self.process_pharmacy_bill
            }
            
            processor = processors.get(function_name)
            if processor:
                return processor(validated_data, user_id)
            else:
                logger.error(f"No processor found for function: {function_name}")
                return False

        except Exception as e:
            logger.error(f"Error in process_doc: {str(e)}", exc_info=True)
            return False


    def process_kyc(self, document) -> Dict[str, Any]:
        try:
            classification_prompt = load_prompt_from_file('config/prompt.json', 'prompt_kyc_classification')

            safety_settings = [
                {
                    "category": "HARM_CATEGORY_DANGEROUS_CONTENT",
                    "threshold": "BLOCK_ONLY_HIGH"
                },
                {
                    "category": "HARM_CATEGORY_HARASSMENT",
                    "threshold": "BLOCK_ONLY_HIGH"
                },
                {
                    "category": "HARM_CATEGORY_HATE_SPEECH",
                    "threshold": "BLOCK_ONLY_HIGH"
                },
                {
                    "category": "HARM_CATEGORY_SEXUALLY_EXPLICIT",
                    "threshold": "BLOCK_ONLY_HIGH"
                }
            ]

            response = self.model.generate_content(
                [document, classification_prompt],
                safety_settings=safety_settings,
                generation_config={"temperature": 0.1},
                tools=[genai.protos.Tool(function_declarations=self.kyc_function_declarations)]
            )

            logger.info(f"Function Calling Response: {response}")

            function_name, extracted_data = self.extract_function_call(response)

            if not function_name or not extracted_data:
                return None

            if function_name not in ["process_prescription", "process_aadhar_card_front", "process_aadhar_card_back", "process_pan_card", "process_bank_account"]:
                logger.error(f"Invalid function name for KYC document: {function_name}")
                return None

            is_valid, validated_data = self.validate_extracted_data(function_name, extracted_data)
            if not is_valid:
                logger.error(f"Invalid data extracted for {function_name}: {extracted_data}")
                return None

            processors = {
                "process_prescription": self.process_prescription,
                "process_aadhar_card_front": self.process_aadhar_card_front,
                "process_aadhar_card_back": self.process_aadhar_card_back,
                "process_pan_card": self.process_pan_card,
                "process_bank_account": self.process_bank_account
            }
            
            processor = processors.get(function_name)
            if processor:
                return processor(validated_data)
            else:
                logger.error(f"No processor found for function: {function_name}")
                return None

        except Exception as e:
            logger.error(f"Error in process_kyc_doc: {str(e)}", exc_info=True)
            return None


    def validate_extracted_data(self, function_name: str, data: Dict[str, Any]) -> Tuple[bool, Dict[str, Any]]:
        """
        Validate the extracted data based on the function name.

        Args:
            function_name (str): The name of the function to validate data for.
            data (Dict[str, Any]): The extracted data to validate.

        Returns:
            Tuple[bool, Dict[str, Any]]: A tuple containing a boolean indicating whether the data is valid and the validated data.
        """
        validated_data = data.copy()

        required_fields = {
            "process_discharge_bill": ["patient_name", "doctor_name", "total_amount", "date", "hospital_name"],
            "process_pharmacy_bill": ["patient_name", "total_amount", "date"],
            "process_prescription": ["patient_name", "doctor_name", "hospital_name"],
            "process_aadhar_card_front": ["name", "aadhar_number", "date_of_birth", "gender"],
            "process_aadhar_card_back": ["street", "city", "state", "pincode"],
            "process_pan_card": ["name", "pan_number"],
            "process_bank_account": ["account_number", "ifsc_code", "bank_name", "branch_name", "account_holder_name"]
        }

        if function_name not in required_fields:
            return False, {}

        for field in required_fields[function_name]:
            if field not in data:
                logger.error(f"Missing required field '{field}' for {function_name}")
                return False, {}

        # Additional type checks
        if function_name in ["process_discharge_bill", "process_pharmacy_bill"]:
            try:
                float(data.get("total_amount"))
            except ValueError:
                logger.error(f"Invalid numeric value in {function_name} data")
                return False, {}

        if function_name == "process_aadhar_card_front":
            # Remove any spaces from the Aadhar number before validation
            aadhar_number = data.get("aadhar_number", "").replace(" ", "")
            if not re.match(r'^\d{12}$', aadhar_number):
                logger.error("Aadhar number should be 12 digits")
                return False, {}
            # Update the validated data with the cleaned Aadhar number
            validated_data["aadhar_number"] = aadhar_number
            
            # Validate and standardize DOB
            try:
                dob_str = data.get("date_of_birth", "")
                # Try parsing with multiple formats
                for fmt in ("%d %b %Y", "%d/%m/%Y", "%Y-%m-%d", "%d-%m-%Y", "%Y-%d-%m"):
                    try:
                        dob = datetime.strptime(dob_str, fmt).date()
                        break
                    except ValueError:
                        continue
                else:
                    raise ValueError("No valid date format found")
                
                today = date.today()
                if dob > today:
                    logger.error("Date of birth cannot be in the future")
                    return False, {}
                age = today.year - dob.year - ((today.month, today.day) < (dob.month, dob.day))
                if age < 1 or age > 100:
                    logger.error("Patient's age must be between 1 and 100 years")
                    return False, {}
                
                validated_data["date_of_birth"] = dob.isoformat()  # Store as ISO 8601 format
            except ValueError as e:
                logger.error(f"Invalid date format for date of birth: {e}")
                return False, {}

        if function_name == "process_pan_card":
            if not re.match(r'^[A-Z]{5}[0-9]{4}[A-Z]$', data.get("pan_number", "")):
                logger.error("PAN number should be 10 characters in the format ABCDE1234F")
                return False, {}

        if function_name == "process_bank_account":
            if not re.match(r'^\d{9,18}$', data.get("account_number", "")):
                logger.error("Bank account number should be between 9 to 18 digits")
                return False, {}
            
            if not re.match(r'^[A-Z]{4}0[A-Z0-9]{6}$', data.get("ifsc_code", "")):
                logger.error("IFSC code should be 11 characters in the format ABCD0123456")
                return False, {}

        return True, validated_data


    def process_discharge_bill(self, data: Dict[str, Any], user_id: str) -> bool:
        try:
            verification_message, verification_status = DocVerifier.verify_discharge_bill_data(data, user_id)
            
            # Try parsing the date with multiple formats
            date_formats = ["%Y-%m-%d", "%d/%m/%Y", "%m/%d/%Y", "%d-%m-%Y", "%Y-%d-%m"]
            parsed_date = None
            for date_format in date_formats:
                try:
                    parsed_date = datetime.strptime(data["date"], date_format)
                    break
                except ValueError:
                    continue
            
            if parsed_date is None:
                raise ValueError(f"Unable to parse date: {data['date']}")

            bill_create = BillCreate(
                    patient_id=user_id,
                    date=parsed_date.isoformat(),
                    amount=float(data["total_amount"]),
                    status=BillStatus.VERIFIED if verification_status else BillStatus.REJECTED,
                    reasoning=verification_message,
                    type=BillType.DISCHARGE
            )
            bill_id = add_data_to_firestore(bill_create.model_dump(), 'bills')
            logger.info(f"Discharge Bill created with ID: {bill_id}")

            return verification_status
        except Exception as e:
            logger.error(f"Error processing discharge bill: {str(e)}")
            return False


    def process_pharmacy_bill(self, data: Dict[str, Any], user_id: str) -> bool:
        try:
            verification_message, verification_status = DocVerifier.verify_pharmacy_bill_data(data, user_id)
            
            # Try parsing the date with multiple formats
            date_formats = ["%Y-%m-%d", "%d/%m/%Y", "%m/%d/%Y", "%d-%m-%Y", "%Y-%d-%m"]
            parsed_date = None
            for date_format in date_formats:
                try:
                    parsed_date = datetime.strptime(data["date"], date_format)
                    break
                except ValueError:
                    continue
            
            if parsed_date is None:
                raise ValueError(f"Unable to parse date: {data['date']}")

            bill_create = BillCreate(
                    patient_id=user_id,
                    date=parsed_date.isoformat(),
                    amount=float(data["total_amount"]),
                    status=BillStatus.VERIFIED if verification_status else BillStatus.REJECTED,
                    reasoning=verification_message,
                    type=BillType.PHARMACY
            )
            bill_id = add_data_to_firestore(bill_create.model_dump(), 'bills')
            logger.info(f"Pharmacy Bill created with ID: {bill_id}")

            return verification_status
        except Exception as e:
            logger.error(f"Error processing pharmacy bill: {str(e)}")
            return False


    def process_prescription(self, data: Dict[str, Any]) -> Dict[str, Any]:
        return {
            "prescription_data": {
                "patient_name": data["patient_name"],
                "doctor_name": data["doctor_name"],
                "hospital_name": data["hospital_name"]
            }
        }

    def process_aadhar_card_front(self, data: Dict[str, Any]) -> Dict[str, Any]:
        return {
            "aadhar_front_data": {
                "aadhar_number": data['aadhar_number'],
                "name": data['name'],
                "date_of_birth": data['date_of_birth'],
                "gender": data['gender'].lower()
            }
        }

    def process_aadhar_card_back(self, data: Dict[str, Any]) -> Dict[str, Any]:
        return {
            "aadhar_back_data": {
                "address": {
                    "street": data['street'],
                    "city": data['city'],
                    "state": data['state'],
                    "pincode": data['pincode']
                }
            }
        }

    def process_pan_card(self, data: Dict[str, Any]) -> Dict[str, Any]:
        return {
            "pan_data": {
                "pan_number": data['pan_number'],
                "name": data['name']
            }
        }

    def process_bank_account(self, data: Dict[str, Any]) -> Dict[str, Any]:
        return {
            "bank_account_data": {
                "account_number": data['account_number'],
                "ifsc_code": data['ifsc_code'],
                "bank_name": data['bank_name'],
                "branch_name": data['branch_name'],
                "account_holder_name": data['account_holder_name']
            }
        }

    def process_unrecognized_document(self, data: Dict[str, Any], user_id: str) -> bool:
        logger.info(f"Unrecognized document detected for user {user_id}")
        logger.info(f"Document content: {data.get('content', 'No content provided')}")
        return False