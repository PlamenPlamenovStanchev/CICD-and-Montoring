package main

import (
	"testing"
)

// TestValidFinishedStatuses tests all statuses that should return "Finished"
func TestValidFinishedStatuses(t *testing.T) {
	finishedStatuses := []string{"Completed", "Archived"}

	for _, status := range finishedStatuses {
		result := CategorizeTask(status)
		if result != "Finished" {
			t.Errorf("Status %q should return 'Finished', got %q", status, result)
		}
	}
}

// TestValidActiveStatuses tests various statuses that should return "Active"
func TestValidActiveStatuses(t *testing.T) {
	activeStatuses := []string{
		"Pending",
		"In Progress",
		"Started",
		"New",
		"Waiting",
	}

	for _, status := range activeStatuses {
		result := CategorizeTask(status)
		if result != "Active" {
			t.Errorf("Status %q should return 'Active', got %q", status, result)
		}
	}
}

// TestNilAndEmptyStatus tests edge cases with empty values
func TestNilAndEmptyStatus(t *testing.T) {
	result := CategorizeTask("")
	if result != "Active" {
		t.Errorf("Empty string should return 'Active', got %q", result)
	}
}
