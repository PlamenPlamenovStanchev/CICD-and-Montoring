package main

import (
	"testing"
)

// TestCategorizeTaskBasicCases tests the most common scenarios
func TestCategorizeTaskBasicCases(t *testing.T) {
	tests := []struct {
		name     string
		status   string
		expected string
	}{
		{
			name:     "Completed task should be Finished",
			status:   "Completed",
			expected: "Finished",
		},
		{
			name:     "Archived task should be Finished",
			status:   "Archived",
			expected: "Finished",
		},
		{
			name:     "Pending task should be Active",
			status:   "Pending",
			expected: "Active",
		},
		{
			name:     "Empty status should be Active",
			status:   "",
			expected: "Active",
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			result := CategorizeTask(tt.status)
			if result != tt.expected {
				t.Errorf("got %q, want %q", result, tt.expected)
			}
		})
	}
}

// TestCategorizeTaskReturnsString verifies the function returns a string
func TestCategorizeTaskReturnsString(t *testing.T) {
	result := CategorizeTask("Completed")
	if result == "" {
		t.Error("CategorizeTask should not return empty string")
	}
}
