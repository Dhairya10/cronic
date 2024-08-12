import os
import asyncio
import google.generativeai as genai
from pydantic_settings import BaseSettings
from dotenv import load_dotenv
from concurrent.futures import ThreadPoolExecutor, ProcessPoolExecutor

load_dotenv()

class Settings(BaseSettings):
    ENVIRONMENT: str = os.getenv("ENVIRONMENT")
    MODEL_NAME: str = os.getenv("MODEL_NAME")
    GEMINI_API_KEY: str = os.getenv("GEMINI_API_KEY")
    FIREBASE_CREDENTIALS_PATH: str = os.getenv("FIREBASE_CREDENTIALS_PATH")
    FIREBASE_STORAGE_BUCKET: str = os.getenv("FIREBASE_STORAGE_BUCKET")
    PROCESSING_TIMEOUT: int = int(os.getenv("PROCESSING_TIMEOUT"))

    # Executor configurations
    THREAD_POOL_MAX_WORKERS: int = min(32, 5 * os.cpu_count())
    PROCESS_POOL_MAX_WORKERS: int = 4

    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"

settings = Settings()

# Declare executors and semaphore
thread_pool: ThreadPoolExecutor = None
process_pool: ProcessPoolExecutor = None
file_processing_semaphore = asyncio.Semaphore(10)

def initialize_pools():
    global thread_pool, process_pool
    thread_pool = ThreadPoolExecutor(max_workers=settings.THREAD_POOL_MAX_WORKERS)
    process_pool = ProcessPoolExecutor(max_workers=settings.PROCESS_POOL_MAX_WORKERS)

def initialize_model():
    genai.configure(api_key=settings.GEMINI_API_KEY)
    return genai.GenerativeModel(model_name=settings.MODEL_NAME)