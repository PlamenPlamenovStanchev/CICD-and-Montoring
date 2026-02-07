package com.example.archiver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.*;

/**
 * Integration tests for complete archiving workflow scenarios.
 * Tests end-to-end archiving behavior with various task states and conditions.
 */
@ExtendWith(MockitoExtension.class)
class ArchiverWorkflowIntegrationTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @InjectMocks
    private TaskArchiver archiver;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private TaskProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new TaskProcessor();
        archiver = new TaskArchiver();
        // Use reflection or setter to inject dependencies
        try {
            java.lang.reflect.Field redisField = TaskArchiver.class.getDeclaredField("redisTemplate");
            redisField.setAccessible(true);
            redisField.set(archiver, redisTemplate);
            
            java.lang.reflect.Field processorField = TaskArchiver.class.getDeclaredField("processor");
            processorField.setAccessible(true);
            processorField.set(archiver, processor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldArchiveOnlyCompletedTasksInMixedWorkload() {
        // Arrange - Simulate a real-world scenario with mixed task states
        Set<String> keys = new HashSet<>();
        keys.add("task:completed-1");
        keys.add("task:pending-1");
        keys.add("task:completed-2");
        keys.add("task:in-progress-1");
        keys.add("task:completed-3");
        
        String completedTask1 = "{\"id\": \"completed-1\", \"title\": \"Deploy to production\", \"status\": \"Completed\", \"priority\": \"High\"}";
        String pendingTask = "{\"id\": \"pending-1\", \"title\": \"Code review\", \"status\": \"Pending\", \"priority\": \"Medium\"}";
        String completedTask2 = "{\"id\": \"completed-2\", \"title\": \"Update docs\", \"status\": \"Completed\", \"priority\": \"Low\"}";
        String inProgressTask = "{\"id\": \"in-progress-1\", \"title\": \"Fix bug\", \"status\": \"In Progress\", \"priority\": \"High\"}";
        String completedTask3 = "{\"id\": \"completed-3\", \"title\": \"Write tests\", \"status\": \"Completed\", \"priority\": \"High\"}";
        
        when(redisTemplate.keys("task:*")).thenReturn(keys);
        when(valueOperations.get("task:completed-1")).thenReturn(completedTask1);
        when(valueOperations.get("task:pending-1")).thenReturn(pendingTask);
        when(valueOperations.get("task:completed-2")).thenReturn(completedTask2);
        when(valueOperations.get("task:in-progress-1")).thenReturn(inProgressTask);
        when(valueOperations.get("task:completed-3")).thenReturn(completedTask3);

        // Act
        archiver.archiveCompletedTasks();

        // Assert - Only completed tasks should be archived
        verify(valueOperations).set("archive:task:completed-1", completedTask1);
        verify(redisTemplate).delete("task:completed-1");
        
        verify(valueOperations).set("archive:task:completed-2", completedTask2);
        verify(redisTemplate).delete("task:completed-2");
        
        verify(valueOperations).set("archive:task:completed-3", completedTask3);
        verify(redisTemplate).delete("task:completed-3");
        
        // Pending and in-progress tasks should NOT be archived
        verify(redisTemplate, never()).delete("task:pending-1");
        verify(redisTemplate, never()).delete("task:in-progress-1");
    }

    @Test
    void shouldHandleSubsequentArchivingRuns() {
        // First run - archive completed tasks
        Set<String> firstRunKeys = new HashSet<>();
        firstRunKeys.add("task:1");
        firstRunKeys.add("task:2");
        
        String task1 = "{\"id\": \"1\", \"status\": \"Completed\"}";
        String task2 = "{\"id\": \"2\", \"status\": \"Pending\"}";
        
        when(redisTemplate.keys("task:*")).thenReturn(firstRunKeys);
        when(valueOperations.get("task:1")).thenReturn(task1);
        when(valueOperations.get("task:2")).thenReturn(task2);

        archiver.archiveCompletedTasks();

        // Second run - task:2 is now completed, new task added
        Set<String> secondRunKeys = new HashSet<>();
        secondRunKeys.add("task:2");
        secondRunKeys.add("task:3");
        
        String task2Updated = "{\"id\": \"2\", \"status\": \"Completed\"}";
        String task3 = "{\"id\": \"3\", \"status\": \"Pending\"}";
        
        when(redisTemplate.keys("task:*")).thenReturn(secondRunKeys);
        when(valueOperations.get("task:2")).thenReturn(task2Updated);
        when(valueOperations.get("task:3")).thenReturn(task3);

        archiver.archiveCompletedTasks();

        // Assert - Both runs should work correctly
        verify(valueOperations).set("archive:task:1", task1);
        verify(valueOperations).set("archive:task:2", task2Updated);
        verify(redisTemplate, times(1)).delete("task:1");
        verify(redisTemplate, times(1)).delete("task:2");
        verify(redisTemplate, never()).delete("task:3");
    }

    @Test
    void shouldHandleHighPriorityCompletedTasks() {
        // Arrange - Test that high priority tasks are archived like any other
        Set<String> keys = new HashSet<>();
        keys.add("task:high-priority");
        
        String highPriorityTask = "{\"id\": \"high-priority\", " +
                "\"title\": \"Critical Security Fix\", " +
                "\"priority\": \"High\", " +
                "\"due_date\": \"2026-02-08\", " +
                "\"status\": \"Completed\"}";
        
        when(redisTemplate.keys("task:*")).thenReturn(keys);
        when(valueOperations.get("task:high-priority")).thenReturn(highPriorityTask);

        // Act
        archiver.archiveCompletedTasks();

        // Assert
        verify(valueOperations).set("archive:task:high-priority", highPriorityTask);
        verify(redisTemplate).delete("task:high-priority");
    }

    @Test
    void shouldPreserveArchivedTaskData() {
        // Arrange - Ensure archived data matches original
        Set<String> keys = new HashSet<>();
        keys.add("task:preserve-test");
        
        String originalTask = "{\"id\": \"preserve-test\", " +
                "\"title\": \"Test Data Preservation\", " +
                "\"priority\": \"Medium\", " +
                "\"due_date\": \"2026-02-15\", " +
                "\"status\": \"Completed\", " +
                "\"created_at\": \"2026-02-01T10:00:00Z\", " +
                "\"metadata\": {\"tags\": [\"important\", \"urgent\"]}}";
        
        when(redisTemplate.keys("task:*")).thenReturn(keys);
        when(valueOperations.get("task:preserve-test")).thenReturn(originalTask);

        // Act
        archiver.archiveCompletedTasks();

        // Assert - Verify exact data is preserved
        verify(valueOperations).set("archive:task:preserve-test", originalTask);
    }

    @Test
    void shouldHandleEmptyDatabaseGracefully() {
        // Arrange
        when(redisTemplate.keys("task:*")).thenReturn(new HashSet<>());

        // Act - Should not throw exception
        archiver.archiveCompletedTasks();

        // Assert - No operations performed
        verify(valueOperations, never()).get(anyString());
        verify(valueOperations, never()).set(anyString(), anyString());
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void shouldHandleTasksWithVariousStatusFormats() {
        // Arrange - Test both "Completed" and "completed" formats
        Set<String> keys = new HashSet<>();
        keys.add("task:upper");
        keys.add("task:lower");
        
        String upperCaseStatus = "{\"id\": \"upper\", \"status\": \"Completed\"}";
        String lowerCaseStatus = "{\"id\": \"lower\", \"status\": \"completed\"}";
        
        when(redisTemplate.keys("task:*")).thenReturn(keys);
        when(valueOperations.get("task:upper")).thenReturn(upperCaseStatus);
        when(valueOperations.get("task:lower")).thenReturn(lowerCaseStatus);

        // Act
        archiver.archiveCompletedTasks();

        // Assert - Both should be archived
        verify(valueOperations).set("archive:task:upper", upperCaseStatus);
        verify(valueOperations).set("archive:task:lower", lowerCaseStatus);
        verify(redisTemplate).delete("task:upper");
        verify(redisTemplate).delete("task:lower");
    }
}
