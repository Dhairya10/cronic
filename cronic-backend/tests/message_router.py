import pytest
from fastapi.testclient import TestClient
from unittest.mock import Mock, patch
from app import app
from models.messages import MessageType, MessageSender

client = TestClient(app)

@pytest.fixture
def mock_verify_token():
    with patch("auth.verify.verify_token") as mock:
        mock.return_value = {"uid": "test_user_id"}
        yield mock

@pytest.fixture
def mock_text_service():
    with patch("services.text_service.TextService") as mock:
        instance = mock.return_value
        instance.process_text.return_value = "Mocked bot response"
        yield instance

@pytest.fixture
def mock_image_service():
    with patch("services.image_service.ImageService") as mock:
        instance = mock.return_value
        instance.process_image.return_value = {"message": "Mocked image analysis"}
        yield instance

@pytest.fixture
def mock_firestore():
    with patch("firebase_admin.firestore.client") as mock:
        instance = mock.return_value
        instance.collection.return_value.add.return_value = (None, Mock(id="mocked_id"))
        yield instance

def test_get_chat_response_text(mock_verify_token, mock_text_service, mock_firestore):
    response = client.post("/chat", json={
        "content": "Hello",
        "type": "text",
        "user_id": "test_user_id"
    })
    assert response.status_code == 200
    assert response.json()["content"] == "Mocked bot response"
    assert response.json()["type"] == "text"
    assert response.json()["sender"] == "bot"

def test_get_chat_response_image(mock_verify_token, mock_image_service, mock_firestore):
    response = client.post("/chat", json={
        "content": "https://example.com/image.jpg",
        "type": "image",
        "user_id": "test_user_id"
    })
    assert response.status_code == 200
    assert response.json()["content"] == "{'message': 'Mocked image analysis'}"
    assert response.json()["type"] == "text"
    assert response.json()["sender"] == "bot"

def test_get_chat_response_unsupported_type(mock_verify_token):
    response = client.post("/chat", json={
        "content": "Unsupported",
        "type": "unsupported",
        "user_id": "test_user_id"
    })
    assert response.status_code == 400
    assert response.json()["detail"] == "Unsupported message type"

@pytest.fixture
def mock_firestore_query():
    with patch("firebase_admin.firestore.client") as mock:
        instance = mock.return_value
        query_mock = Mock()
        query_mock.stream.return_value = [
            Mock(to_dict=lambda: {
                "id": "msg1",
                "content": "Hello",
                "type": "text",
                "sender": "user",
                "user_id": "test_user_id",
                "timestamp": "2023-01-01T00:00:00"
            })
        ]
        instance.collection.return_value.where.return_value.order_by.return_value.limit.return_value = query_mock
        yield instance

def test_get_chat_history(mock_verify_token, mock_firestore_query):
    response = client.get("/chat/history")
    assert response.status_code == 200
    assert len(response.json()["messages"]) == 1
    assert response.json()["messages"][0]["content"] == "Hello"

def test_get_chat_history_with_invalid_last_message_id(mock_verify_token, mock_firestore_query):
    mock_firestore_query.collection.return_value.document.return_value.get.return_value.exists = False
    response = client.get("/chat/history?last_message_id=invalid_id")
    assert response.status_code == 400
    assert response.json()["detail"] == "Invalid last_message_id"