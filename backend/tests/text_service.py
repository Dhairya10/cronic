import pytest
from unittest.mock import Mock, patch
from services.text_service import TextService

@pytest.fixture
def mock_model():
    model = Mock()
    model.generate_content.return_value = Mock(text="Mocked response")
    return model

@pytest.fixture
def mock_get_chat_history():
    with patch("utils.gemini.get_chat_history") as mock:
        mock.return_value = [
            {'role': 'user', 'parts': [{'text': 'Hello'}]},
            {'role': 'model', 'parts': [{'text': 'Hi there!'}]}
        ]
        yield mock

def test_process_text(mock_model, mock_get_chat_history):
    text_service = TextService(mock_model)
    response = text_service.process_text("test_user_id", "How are you?")
    
    assert response == "Mocked response"
    mock_get_chat_history.assert_called_once_with("test_user_id")
    mock_model.generate_content.assert_called_once()
    call_args = mock_model.generate_content.call_args[0][0]
    assert len(call_args) == 3
    assert call_args[-1] == {'role': 'user', 'parts': [{'text': 'How are you?'}]}

def test_process_text_empty_history(mock_model, mock_get_chat_history):
    mock_get_chat_history.return_value = []
    text_service = TextService(mock_model)
    response = text_service.process_text("test_user_id", "Hello")
    
    assert response == "Mocked response"
    mock_get_chat_history.assert_called_once_with("test_user_id")
    mock_model.generate_content.assert_called_once()
    call_args = mock_model.generate_content.call_args[0][0]
    assert len(call_args) == 1
    assert call_args[0] == {'role': 'user', 'parts': [{'text': 'Hello'}]}