from fastapi import FastAPI
from firebase_admin import credentials, initialize_app
from config.settings import settings
from routers import claim, kyc, patient, bills, hospital
from contextlib import asynccontextmanager
from fastapi.middleware.cors import CORSMiddleware
from config.settings import initialize_pools, initialize_model, thread_pool, process_pool

@asynccontextmanager
async def lifespan(app: FastAPI):

    # Startup: Initialize Firebase and pass executors to the router
    cred = credentials.Certificate(settings.FIREBASE_CREDENTIALS_PATH)
    initialize_app(cred)
    initialize_pools()
    initialize_model()
    
    yield
    
    # Shutdown: Clean up the executors
    if thread_pool is not None:
        thread_pool.shutdown(wait=True)
    if process_pool is not None:
        process_pool.shutdown(wait=True)

app = FastAPI(
    title="Document API",
    description="API for handling KYC and claim documents",
    version="1.0.0",
    lifespan=lifespan
)

# Include the router
app.include_router(claim.router, prefix="/claim", tags=["claim"])
app.include_router(kyc.router, prefix="/kyc", tags=["kyc"])
app.include_router(patient.router, prefix="/patient", tags=["patient"])
app.include_router(bills.router, prefix="/bills", tags=["bills"])
app.include_router(hospital.router, prefix="/hospital", tags=["hospital"])

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Allows all origins
    allow_credentials=True,
    allow_methods=["*"],  # Allows all methods
    allow_headers=["*"],  # Allows all headers
)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8080)