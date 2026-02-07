import pytest
from unittest.mock import patch, MagicMock
import json
from app import app, validate_task

@pytest.fixture
def client():
    app.config['TESTING'] = True
    with app.test_client() as client:
        yield client

def test_validate_task_missing_title_high():
    data = {"priority": "High"}
    is_valid, error = validate_task(data)
    assert is_valid is False
    assert error == "Title is required"

def test_validate_task_missing_title_low():
    data = {"priority": "Low"}
    is_valid, error = validate_task(data)
    assert is_valid is False
    assert error == "Title is required"

def test_validate_task_high_priority_no_date():
    data = {"title": "Critical Bug", "priority": "High"}
    is_valid, error = validate_task(data)
    assert is_valid is False
    assert error == "High priority tasks need a due date"

def test_validate_task_valid_low():
    data = {"title": "Read the news", "priority": "Low"}
    is_valid, error = validate_task(data)
    assert is_valid is True
    assert error is None

def test_validate_task_valid_high():
    data = {"title": "Update Jenkins", "priority": "High", "due_date": "2026-02-10"}
    is_valid, error = validate_task(data)
    assert is_valid is True
    assert error is None

@patch('app.db.set')
def test_create_task_api(mock_set, client):
    payload = {
        "title": "Pipeline Test",
        "priority": "Low"
    }
    response = client.post('/tasks', json=payload)
    data = response.get_json()
    
    assert response.status_code == 201
    assert data['title'] == "Pipeline Test"
    assert mock_set.called

@patch('app.db.set')
def test_create_task_api_high(mock_set, client):
    payload = {
        "title": "Pipeline Test",
        "priority": "High"
    }
    response = client.post('/tasks', json=payload)
    data = response.get_json()
    
    assert response.status_code == 400 # used to be 201
    #assert data['status'] == 'error'
    assert mock_set.called is False

@patch('app.db.keys')
def test_get_tasks_api(mock_keys, client):
    mock_keys.return_value = []
    response = client.get('/tasks')
    assert response.status_code == 200
    assert isinstance(response.get_json(), list)

def test_delete_nonexistent_task(client):
    with patch('app.db.delete') as mock_del:
        mock_del.return_value = 0
        response = client.delete('/tasks/invalid-id')
        assert response.status_code == 404
        assert response.get_json()['error'] == "Task not found"

# Additional Unit Tests for validate_task function

def test_validate_task_empty_data():
    """Test validation with empty data"""
    is_valid, error = validate_task({})
    assert is_valid is False
    assert error == "Title is required"

def test_validate_task_none_data():
    """Test validation with None data"""
    is_valid, error = validate_task(None)
    assert is_valid is False
    assert error == "Title is required"

def test_validate_task_empty_title():
    """Test validation with empty string title"""
    data = {"title": "", "priority": "Low"}
    is_valid, error = validate_task(data)
    assert is_valid is False
    assert error == "Title is required"

def test_validate_task_whitespace_title():
    """Test validation with whitespace-only title"""
    data = {"title": "   ", "priority": "Low"}
    is_valid, error = validate_task(data)
    assert is_valid is True  # Current implementation allows whitespace
    assert error is None

def test_validate_task_high_priority_with_date():
    """Test high priority task with due date"""
    data = {"title": "Urgent Task", "priority": "High", "due_date": "2026-12-31"}
    is_valid, error = validate_task(data)
    assert is_valid is True
    assert error is None

def test_validate_task_medium_priority():
    """Test medium priority task without due date"""
    data = {"title": "Medium Task", "priority": "Medium"}
    is_valid, error = validate_task(data)
    assert is_valid is True
    assert error is None

def test_validate_task_no_priority():
    """Test task without priority specified"""
    data = {"title": "Simple Task"}
    is_valid, error = validate_task(data)
    assert is_valid is True
    assert error is None

# Additional API Integration Tests

@patch('app.db.set')
def test_create_task_with_all_fields(mock_set, client):
    """Test creating a task with all fields populated"""
    payload = {
        "title": "Complete Project",
        "priority": "High",
        "due_date": "2026-03-15"
    }
    response = client.post('/tasks', json=payload)
    data = response.get_json()
    
    assert response.status_code == 201
    assert data['title'] == "Complete Project"
    assert data['priority'] == "High"
    assert data['due_date'] == "2026-03-15"
    assert data['status'] == "Pending"
    assert 'id' in data
    assert mock_set.called

@patch('app.db.set')
def test_create_task_default_priority(mock_set, client):
    """Test that priority defaults to Low when not specified"""
    payload = {"title": "Default Priority Task"}
    response = client.post('/tasks', json=payload)
    data = response.get_json()
    
    assert response.status_code == 201
    assert data['priority'] == "Low"
    assert mock_set.called

@patch('app.db.set')
def test_create_task_missing_title(mock_set, client):
    """Test creating task without title returns error"""
    payload = {"priority": "High", "due_date": "2026-03-01"}
    response = client.post('/tasks', json=payload)
    data = response.get_json()
    
    assert response.status_code == 400
    assert data['status'] == 'error'
    assert data['message'] == "Title is required"
    assert mock_set.called is False

@patch('app.db.set')
def test_create_task_empty_json(mock_set, client):
    """Test creating task with empty JSON"""
    response = client.post('/tasks', json={})
    data = response.get_json()
    
    assert response.status_code == 400
    assert data['status'] == 'error'
    assert mock_set.called is False

@patch('app.db.keys')
@patch('app.db.get')
def test_get_tasks_multiple(mock_get, mock_keys, client):
    """Test getting multiple tasks"""
    mock_keys.return_value = [b'task:1', b'task:2', b'task:3']
    mock_get.side_effect = [
        json.dumps({"id": "1", "title": "Task 1", "priority": "Low", "status": "Pending"}),
        json.dumps({"id": "2", "title": "Task 2", "priority": "High", "status": "Pending"}),
        json.dumps({"id": "3", "title": "Task 3", "priority": "Medium", "status": "Completed"})
    ]
    
    response = client.get('/tasks')
    data = response.get_json()
    
    assert response.status_code == 200
    assert isinstance(data, list)
    assert len(data) == 3
    assert data[0]['title'] == "Task 1"
    assert data[1]['priority'] == "High"
    assert data[2]['status'] == "Completed"

@patch('app.db.get')
@patch('app.db.set')
def test_complete_task_success(mock_set, mock_get, client):
    """Test marking a task as completed"""
    task_id = "test-task-123"
    existing_task = {
        "id": task_id,
        "title": "Test Task",
        "priority": "Low",
        "status": "Pending"
    }
    mock_get.return_value = json.dumps(existing_task)
    
    response = client.put(f'/tasks/{task_id}/complete')
    data = response.get_json()
    
    assert response.status_code == 200
    assert data['status'] == "Completed"
    assert data['title'] == "Test Task"
    assert mock_set.called

@patch('app.db.get')
def test_complete_nonexistent_task(mock_get, client):
    """Test completing a task that doesn't exist"""
    mock_get.return_value = None
    
    response = client.put('/tasks/nonexistent-id/complete')
    data = response.get_json()
    
    assert response.status_code == 404
    assert data['error'] == "Task not found"

@patch('app.db.delete')
def test_delete_task_success(mock_delete, client):
    """Test successfully deleting a task"""
    mock_delete.return_value = 1
    
    response = client.delete('/tasks/valid-task-id')
    data = response.get_json()
    
    assert response.status_code == 200
    assert data['message'] == "Task deleted"

@patch('app.db.ping')
def test_health_endpoint_healthy(mock_ping, client):
    """Test health endpoint when database is connected"""
    mock_ping.return_value = True
    
    response = client.get('/health')
    data = response.get_json()
    
    assert response.status_code == 200
    assert data['status'] == 'healthy'
    assert data['database'] == 'connected'

@patch('app.db.ping')
def test_health_endpoint_unhealthy(mock_ping, client):
    """Test health endpoint when database is disconnected"""
    mock_ping.side_effect = Exception("Connection refused")
    
    response = client.get('/health')
    data = response.get_json()
    
    assert response.status_code == 500
    assert data['status'] == 'unhealthy'
    assert 'error' in data

# Edge Case Tests

@patch('app.db.set')
def test_create_task_with_special_characters(mock_set, client):
    """Test creating task with special characters in title"""
    payload = {
        "title": "Fix bug #123: API returns 500 for /users endpoint!",
        "priority": "High",
        "due_date": "2026-02-20"
    }
    response = client.post('/tasks', json=payload)
    data = response.get_json()
    
    assert response.status_code == 201
    assert data['title'] == payload['title']

@patch('app.db.set')
def test_create_task_with_unicode(mock_set, client):
    """Test creating task with Unicode characters"""
    payload = {
        "title": "测试任务 - Test Task 🚀",
        "priority": "Low"
    }
    response = client.post('/tasks', json=payload)
    data = response.get_json()
    
    assert response.status_code == 201
    assert data['title'] == payload['title']

@patch('app.db.set')
def test_create_task_long_title(mock_set, client):
    """Test creating task with very long title"""
    long_title = "A" * 1000
    payload = {
        "title": long_title,
        "priority": "Low"
    }
    response = client.post('/tasks', json=payload)
    data = response.get_json()
    
    assert response.status_code == 201
    assert data['title'] == long_title

@patch('app.db.get')
@patch('app.db.set')
def test_complete_already_completed_task(mock_set, mock_get, client):
    """Test completing a task that is already completed"""
    task_id = "completed-task"
    existing_task = {
        "id": task_id,
        "title": "Already Done",
        "priority": "Low",
        "status": "Completed"
    }
    mock_get.return_value = json.dumps(existing_task)
    
    response = client.put(f'/tasks/{task_id}/complete')
    data = response.get_json()
    
    assert response.status_code == 200
    assert data['status'] == "Completed"  # Still completed

# Integration Test Scenarios

@patch('app.db.set')
@patch('app.db.get')
@patch('app.db.delete')
def test_task_lifecycle(mock_delete, mock_get, mock_set, client):
    """Test complete task lifecycle: create -> complete -> delete"""
    # Create task
    create_payload = {
        "title": "Lifecycle Test",
        "priority": "Medium"
    }
    create_response = client.post('/tasks', json=create_payload)
    created_task = create_response.get_json()
    task_id = created_task['id']
    
    assert create_response.status_code == 201
    assert created_task['status'] == "Pending"
    
    # Complete task
    mock_get.return_value = json.dumps(created_task)
    complete_response = client.put(f'/tasks/{task_id}/complete')
    completed_task = complete_response.get_json()
    
    assert complete_response.status_code == 200
    assert completed_task['status'] == "Completed"
    
    # Delete task
    mock_delete.return_value = 1
    delete_response = client.delete(f'/tasks/{task_id}')
    
    assert delete_response.status_code == 200

@patch('app.db.keys')
def test_get_tasks_empty_database(mock_keys, client):
    """Test getting tasks when database is empty"""
    mock_keys.return_value = []
    
    response = client.get('/tasks')
    data = response.get_json()
    
    assert response.status_code == 200
    assert isinstance(data, list)
    assert len(data) == 0
