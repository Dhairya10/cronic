from pydantic import BaseModel

class Hospital(BaseModel):
    name: str
    contact_number: str
    address: str
    google_maps_link: str
    website_link: str