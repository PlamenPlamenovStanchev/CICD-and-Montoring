package main

import (
	"testing"
)

// TestCategorizeTaskOutputFormat verifies the output is always valid
func TestCategorizeTaskOutputFormat(t *testing.T) {
	testCases := []string{
		"Completed",
		"Archived",
		"Pending",
		"Random",
		"",
	}

	for _, input := range testCases {
		result := CategorizeTask(input)
		
		// Result should always be one of these two values
		if result != "Finished" && result != "Active" {
			t.Errorf("Unexpected result %q for input %q", result, input)
		}
		
		// Result should never be empty
		if result == "" {
			t.Errorf("CategorizeTask should never return empty string")
		}
	}
}

// TestCategorizeTaskMultipleCalls tests consistency across multiple calls
func TestCategorizeTaskMultipleCalls(t *testing.T) {
	status := "Completed"
	
	// Call the function multiple times with the same input
	firstResult := CategorizeTask(status)
	secondResult := CategorizeTask(status)
	thirdResult := CategorizeTask(status)
	
	// All results should be identical
	if firstResult != secondResult || secondResult != thirdResult {
		t.Errorf("Function returned inconsistent results: %q, %q, %q", 
			firstResult, secondResult, thirdResult)
	}
}
