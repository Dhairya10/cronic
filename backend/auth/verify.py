from fastapi import HTTPException, Depends
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from firebase_admin import auth
from config.logger import logger

security = HTTPBearer()



async def verify_token(credentials: HTTPAuthorizationCredentials = Depends(security)):    
    token = credentials.credentials
    try:
        decoded_token = auth.verify_id_token(token)
        logger.info(f"Token verified successfully for user: {decoded_token.get('uid')}")
        return decoded_token
    except Exception as e:
        logger.error(f"Token verification failed: {str(e)}")
        raise HTTPException(status_code=401, detail=f"Invalid authentication credentials: {e}")