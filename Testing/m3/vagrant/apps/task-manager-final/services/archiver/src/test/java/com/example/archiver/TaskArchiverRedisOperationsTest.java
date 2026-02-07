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
 * Unit tests for TaskArchiver Redis operations.
 * Tests the archiving logic with mocked Redis operations.
 */
@ExtendWith(MockitoExtension.class)
class TaskArchiverRedisOperationsTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private TaskProcessor processor;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private TaskArchiver archiver;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldArchiveCompletedTaskSuccessfully() {
        // Arrange
        Set<String> keys = new HashSet<>();
        keys.add("task:123");
        
        String completedTaskJson = "{\"id\": \"123\", \"status\": \"Completed\"}";
        
        when(redisTemplate.keys("task:*")).thenReturn(keys);
        when(valueOperations.get("task:123")).thenReturn(completedTaskJson);
        when(processor.isArchivable(completedTaskJson)).thenReturn(true);

        // Act
        archiver.archiveCompletedTasks();

        // Assert
        verify(valueOperations).set("archive:task:123", completedTaskJson);
        verify(redisTemplate).delete("task:123");
    }

    @Test
    void shouldNotArchivePendingTask() {
        // Arrange
        Set<String> keys = new HashSet<>();
        keys.add("task:456");
        
        String pendingTaskJson = "{\"id\": \"456\", \"status\": \"Pending\"}";
        
        when(redisTemplate.keys("task:*")).thenReturn(keys);
        when(valueOperations.get("task:456")).thenReturn(pendingTaskJson);
        when(processor.isArchivable(pendingTaskJson)).thenReturn(false);

        // Act
        archiver.archiveCompletedTasks();

        // Assert
        verify(valueOperations, never()).set(startsWith("archive:"), anyString());
        verify(redisTemplate, never()).delete("task:456");
    }

    @Test
    void shouldHandleNoTasksFound() {
        // Arrange
        when(redisTemplate.keys("task:*")).thenReturn(null);

        // Act
        archiver.archiveCompletedTasks();

        // Assert
        verify(valueOperations, never()).get(anyString());
        verify(valueOperations, never()).set(anyString(), anyString());
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void shouldHandleEmptyTaskSet() {
        // Arrange
        Set<String> emptyKeys = new HashSet<>();
        when(redisTemplate.keys("task:*")).thenReturn(emptyKeys);

        // Act
        archiver.archiveCompletedTasks();

        // Assert
        verify(valueOperations, never()).get(anyString());
        verify(valueOperations, never()).set(anyString(), anyString());
    }

    @Test
    void shouldArchiveMultipleCompletedTasks() {
        // Arrange
        Set<String> keys = new HashSet<>();
        keys.add("task:1");
        keys.add("task:2");
        keys.add("task:3");
        
        String completedTask1 = "{\"id\": \"1\", \"status\": \"Completed\"}";
        String pendingTask = "{\"id\": \"2\", \"status\": \"Pending\"}";
        String completedTask2 = "{\"id\": \"3\", \"status\": \"Completed\"}";
        
        when(redisTemplate.keys("task:*")).thenReturn(keys);
        when(valueOperations.get("task:1")).thenReturn(completedTask1);
        when(valueOperations.get("task:2")).thenReturn(pendingTask);
        when(valueOperations.get("task:3")).thenReturn(completedTask2);
        
        when(processor.isArchivable(completedTask1)).thenReturn(true);
        when(processor.isArchivable(pendingTask)).thenReturn(false);
        when(processor.isArchivable(completedTask2)).thenReturn(true);

        // Act
        archiver.archiveCompletedTasks();

        // Assert
        verify(valueOperations).set("archive:task:1", completedTask1);
        verify(redisTemplate).delete("task:1");
        verify(valueOperations).set("archive:task:3", completedTask2);
        verify(redisTemplate).delete("task:3");
        verify(redisTemplate, never()).delete("task:2");
    }

    @Test
    void shouldHandleNullTaskJson() {
        // Arrange
        Set<String> keys = new HashSet<>();
        keys.add("task:null-task");
        
        when(redisTemplate.keys("task:*")).thenReturn(keys);
        when(valueOperations.get("task:null-task")).thenReturn(null);
        when(processor.isArchivable(null)).thenReturn(false);

        // Act
        archiver.archiveCompletedTasks();

        // Assert
        verify(valueOperations, never()).set(startsWith("archive:"), anyString());
        verify(redisTemplate, never()).delete("task:null-task");
    }

    @Test
    void shouldProcessLargeNumberOfTasks() {
        // Arrange
        Set<String> keys = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            keys.add("task:" + i);
        }
        
        String completedJson = "{\"status\": \"Completed\"}";
        
        when(redisTemplate.keys("task:*")).thenReturn(keys);
        when(valueOperations.get(anyString())).thenReturn(completedJson);
        when(processor.isArchivable(completedJson)).thenReturn(true);

        // Act
        archiver.archiveCompletedTasks();

        // Assert
        verify(valueOperations, times(100)).set(startsWith("archive:task:"), eq(completedJson));
        verify(redisTemplate, times(100)).delete(startsWith("task:"));
    }
}
