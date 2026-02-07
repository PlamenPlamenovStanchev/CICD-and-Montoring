package com.example.archiver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Extended unit tests for TaskProcessor edge cases.
 * Tests JSON parsing logic and various status formats.
 */
class TaskProcessorEdgeCaseTest {

    private TaskProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new TaskProcessor();
    }

    @Test
    void shouldRecognizeCompletedWithExtraSpaces() {
        String json = "{\"id\": \"123\", \"status\":  \"Completed\" }";
        assertTrue(processor.isArchivable(json), "Should handle extra spaces");
    }

    @Test
    void shouldRecognizeLowercaseCompleted() {
        String json = "{\"id\": \"123\", \"status\": \"completed\"}";
        assertTrue(processor.isArchivable(json), "Should handle lowercase status");
    }

    @Test
    void shouldNotArchiveInProgressStatus() {
        String json = "{\"id\": \"123\", \"status\": \"In Progress\"}";
        assertFalse(processor.isArchivable(json), "Should not archive in-progress tasks");
    }

    @Test
    void shouldNotArchiveCancelledStatus() {
        String json = "{\"id\": \"123\", \"status\": \"Cancelled\"}";
        assertFalse(processor.isArchivable(json), "Should not archive cancelled tasks");
    }

    @Test
    void shouldHandleComplexJsonWithMultipleFields() {
        String json = "{\n" +
                "  \"id\": \"task-999\",\n" +
                "  \"title\": \"Complex Task\",\n" +
                "  \"priority\": \"High\",\n" +
                "  \"due_date\": \"2026-02-20\",\n" +
                "  \"status\": \"Completed\",\n" +
                "  \"created_at\": \"2026-02-01\"\n" +
                "}";
        assertTrue(processor.isArchivable(json), "Should handle complex JSON");
    }

    @Test
    void shouldHandleJsonWithCompletedInTitle() {
        String json = "{\"title\": \"Completed documentation review\", \"status\": \"Pending\"}";
        assertFalse(processor.isArchivable(json), 
            "Should not confuse 'Completed' in title with status");
    }

    @Test
    void shouldHandleWhitespaceOnlyString() {
        String json = "   \n\t  ";
        assertFalse(processor.isArchivable(json), "Should reject whitespace-only strings");
    }

    @Test
    void shouldHandleJsonWithNoStatus() {
        String json = "{\"id\": \"123\", \"title\": \"No Status Task\"}";
        assertFalse(processor.isArchivable(json), "Should handle JSON without status field");
    }

    @Test
    void shouldHandleMalformedJson() {
        String json = "{incomplete json";
        assertFalse(processor.isArchivable(json), "Should safely handle malformed JSON");
    }

    @Test
    void shouldHandleJsonArray() {
        String json = "[{\"status\": \"Completed\"}, {\"status\": \"Pending\"}]";
        assertTrue(processor.isArchivable(json), 
            "Should find Completed in array (current implementation)");
    }

    @Test
    void shouldNotArchiveEmptyJsonObject() {
        String json = "{}";
        assertFalse(processor.isArchivable(json), "Should not archive empty JSON object");
    }

    @Test
    void shouldHandleVeryLongJsonString() {
        StringBuilder longJson = new StringBuilder("{\"id\": \"long-task\",");
        longJson.append("\"description\": \"");
        for (int i = 0; i < 1000; i++) {
            longJson.append("Very long description text. ");
        }
        longJson.append("\", \"status\": \"Completed\"}");
        
        assertTrue(processor.isArchivable(longJson.toString()), 
            "Should handle very long JSON strings");
    }

    @Test
    void shouldHandleJsonWithEscapedQuotes() {
        String json = "{\"id\": \"123\", \"title\": \"Task with \\\"quotes\\\"\", \"status\": \"Completed\"}";
        assertTrue(processor.isArchivable(json), "Should handle escaped quotes in JSON");
    }

    @Test
    void shouldHandleMultipleStatusOccurrences() {
        String json = "{\"previous_status\": \"Pending\", \"status\": \"Completed\"}";
        assertTrue(processor.isArchivable(json), 
            "Should recognize completed status even with multiple status fields");
    }
}
