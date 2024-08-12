import requests
from fastapi import HTTPException, Depends
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from firebase_admin import auth
from config.settings import settings
from config.logger import logger

security = HTTPBearer()


def generate_dummy_token(uid="test_user"):
    try:
        custom_token = auth.create_custom_token(uid)
        return custom_token.decode('utf-8')
    except Exception as e:
        print(f"Error generating custom token: {e}")
        return None
    

def exchange_custom_token_for_id_token(custom_token, api_key):
    url = f"https://identitytoolkit.googleapis.com/v1/accounts:signInWithCustomToken?key={api_key}"
    payload = {
        "token": custom_token,
        "returnSecureToken": True
    }
    response = requests.post(url, json=payload)
    if response.status_code == 200:
        return response.json()['idToken']
    else:
        print(f"Error exchanging token: {response.text}")
        return None


async def verify_token(credentials: HTTPAuthorizationCredentials = Depends(security)):
    logger.info(f"Verifying token. Environment: {settings.ENVIRONMENT}")

    if settings.ENVIRONMENT == "local":
        logger.info("Using local environment, returning dummy user ID")
        return {"uid": "rkuRc2Er0wTILi0IAXuA2ypEil63"}
    
    token = credentials.credentials
    try:
        decoded_token = auth.verify_id_token(token)
        logger.info(f"Token verified successfully for user: {decoded_token.get('uid')}")
        return decoded_token
    except Exception as e:
        logger.error(f"Token verification failed: {str(e)}")
        raise HTTPException(status_code=401, detail=f"Invalid authentication credentials: {e}")