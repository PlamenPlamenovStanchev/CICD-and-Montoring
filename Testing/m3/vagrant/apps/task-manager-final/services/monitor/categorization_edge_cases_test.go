package main

import (
	"testing"
)

// TestCategorizeTaskEdgeCases tests edge cases and various input scenarios
// for the task categorization logic
func TestCategorizeTaskEdgeCases(t *testing.T) {
	tests := []struct {
		name     string
		status   string
		expected string
	}{
		{
			name:     "Completed status returns Finished",
			status:   "Completed",
			expected: "Finished",
		},
		{
			name:     "Archived status returns Finished",
			status:   "Archived",
			expected: "Finished",
		},
		{
			name:     "Pending status returns Active",
			status:   "Pending",
			expected: "Active",
		},
		{
			name:     "In Progress status returns Active",
			status:   "In Progress",
			expected: "Active",
		},
		{
			name:     "Empty string returns Active",
			status:   "",
			expected: "Active",
		},
		{
			name:     "Lowercase completed returns Active",
			status:   "completed",
			expected: "Active",
		},
		{
			name:     "Lowercase archived returns Active",
			status:   "archived",
			expected: "Active",
		},
		{
			name:     "Uppercase COMPLETED returns Active",
			status:   "COMPLETED",
			expected: "Active",
		},
		{
			name:     "Mixed case CompLeted returns Active",
			status:   "CompLeted",
			expected: "Active",
		},
		{
			name:     "Started status returns Active",
			status:   "Started",
			expected: "Active",
		},
		{
			name:     "Cancelled status returns Active",
			status:   "Cancelled",
			expected: "Active",
		},
		{
			name:     "On Hold status returns Active",
			status:   "On Hold",
			expected: "Active",
		},
		{
			name:     "Blocked status returns Active",
			status:   "Blocked",
			expected: "Active",
		},
		{
			name:     "Status with leading whitespace returns Active",
			status:   " Completed",
			expected: "Active",
		},
		{
			name:     "Status with trailing whitespace returns Active",
			status:   "Completed ",
			expected: "Active",
		},
		{
			name:     "Status with extra spaces returns Active",
			status:   "  Completed  ",
			expected: "Active",
		},
		{
			name:     "Unknown status returns Active",
			status:   "SomeUnknownStatus",
			expected: "Active",
		},
		{
			name:     "Numeric status returns Active",
			status:   "12345",
			expected: "Active",
		},
		{
			name:     "Special characters returns Active",
			status:   "!@#$%",
			expected: "Active",
		},
		{
			name:     "Very long status string returns Active",
			status:   "ThisIsAVeryLongStatusStringThatDoesNotMatchAnyKnownStatus",
			expected: "Active",
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			result := CategorizeTask(tt.status)
			if result != tt.expected {
				t.Errorf("CategorizeTask(%q) = %q; expected %q", tt.status, result, tt.expected)
			}
		})
	}
}

// TestCategorizeTaskWithJSONFragments tests categorization with partial JSON strings
func TestCategorizeTaskWithJSONFragments(t *testing.T) {
	tests := []struct {
		name     string
		input    string
		expected string
	}{
		{
			name:     "JSON with Completed status",
			input:    `{"status": "Completed", "id": "123"}`,
			expected: "Active", // Function only checks exact string match, not JSON parsing
		},
		{
			name:     "JSON with Archived status",
			input:    `{"status": "Archived", "id": "456"}`,
			expected: "Active",
		},
		{
			name:     "Plain Completed string",
			input:    "Completed",
			expected: "Finished",
		},
		{
			name:     "Plain Archived string",
			input:    "Archived",
			expected: "Finished",
		},
		{
			name:     "Multiline JSON",
			input:    "{\n  \"status\": \"Completed\"\n}",
			expected: "Active",
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			result := CategorizeTask(tt.input)
			if result != tt.expected {
				t.Errorf("CategorizeTask(%q) = %q; expected %q", tt.input, result, tt.expected)
			}
		})
	}
}

// TestCategorizeTaskConsistency ensures the function always returns one of two values
func TestCategorizeTaskConsistency(t *testing.T) {
	testInputs := []string{
		"Completed",
		"Archived",
		"Pending",
		"In Progress",
		"",
		"Random",
		"123",
		"completed",
		"ARCHIVED",
		"  Completed  ",
	}

	for _, input := range testInputs {
		result := CategorizeTask(input)
		if result != "Finished" && result != "Active" {
			t.Errorf("CategorizeTask(%q) returned unexpected value: %q. Expected 'Finished' or 'Active'", input, result)
		}
	}
}

// BenchmarkCategorizeTask benchmarks the categorization function
func BenchmarkCategorizeTask(b *testing.B) {
	for i := 0; i < b.N; i++ {
		CategorizeTask("Completed")
	}
}

// BenchmarkCategorizeTaskActive benchmarks with active status
func BenchmarkCategorizeTaskActive(b *testing.B) {
	for i := 0; i < b.N; i++ {
		CategorizeTask("Pending")
	}
}
