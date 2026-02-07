package com.example.archiver;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Task model validation and construction.
 * Tests the Task POJO for proper getter/setter functionality
 * and object creation patterns.
 */
class TaskModelValidationTest {

    @Test
    void shouldCreateTaskWithAllFields() {
        Task task = new Task(
            "task-123",
            "Complete Documentation",
            "High",
            "2026-02-15",
            "Completed"
        );

        assertEquals("task-123", task.getId());
        assertEquals("Complete Documentation", task.getTitle());
        assertEquals("High", task.getPriority());
        assertEquals("2026-02-15", task.getDueDate());
        assertEquals("Completed", task.getStatus());
    }

    @Test
    void shouldCreateEmptyTaskWithNoArgsConstructor() {
        Task task = new Task();
        
        assertNotNull(task, "Task should be instantiated");
        assertNull(task.getId(), "ID should be null for empty task");
        assertNull(task.getTitle(), "Title should be null for empty task");
        assertNull(task.getStatus(), "Status should be null for empty task");
    }

    @Test
    void shouldHandleNullValues() {
        Task task = new Task(null, null, null, null, null);
        
        assertNull(task.getId());
        assertNull(task.getTitle());
        assertNull(task.getPriority());
        assertNull(task.getDueDate());
        assertNull(task.getStatus());
    }

    @Test
    void shouldCreatePendingTask() {
        Task task = new Task(
            "task-456",
            "Review Pull Request",
            "Medium",
            "2026-02-20",
            "Pending"
        );

        assertEquals("Pending", task.getStatus());
        assertNotEquals("Completed", task.getStatus());
    }

    @Test
    void shouldHandleSpecialCharactersInFields() {
        Task task = new Task(
            "task-789",
            "Fix bug #123: API returns 500 for /users endpoint!",
            "High",
            "2026-02-10",
            "Completed"
        );

        assertTrue(task.getTitle().contains("#123"));
        assertTrue(task.getTitle().contains("/users"));
        assertTrue(task.getTitle().contains("!"));
    }

    @Test
    void shouldHandleUnicodeCharacters() {
        Task task = new Task(
            "task-unicode-1",
            "测试任务 - Test Task 🚀",
            "Low",
            "2026-03-01",
            "Pending"
        );

        assertNotNull(task.getTitle());
        assertTrue(task.getTitle().contains("测试任务"));
        assertTrue(task.getTitle().contains("🚀"));
    }
}
