import logging

def setup_logging():
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
        handlers=[
            logging.StreamHandler()  # This will output to console only
        ]
    )

    # Set logging level for Firebase
    # logging.getLogger("firebase_admin").setLevel(logging.WARNING)

setup_logging()
logger = logging.getLogger(__name__)