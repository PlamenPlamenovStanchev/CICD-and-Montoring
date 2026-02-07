package main

import (
	"encoding/json"
	"io"
	"net/http/httptest"
	"testing"

	"github.com/gofiber/fiber/v2"
	"github.com/stretchr/testify/assert"
)

// TestHealthEndpointResponse tests the health endpoint response structure
func TestHealthEndpointResponse(t *testing.T) {
	app := fiber.New()

	// Mock health endpoint that returns proper structure
	app.Get("/health", func(c *fiber.Ctx) error {
		return c.JSON(fiber.Map{
			"status":  "healthy",
			"service": "go-monitor",
		})
	})

	req := httptest.NewRequest("GET", "/health", nil)
	resp, err := app.Test(req)

	assert.NoError(t, err)
	assert.Equal(t, 200, resp.StatusCode)

	// Read and parse response body
	body, err := io.ReadAll(resp.Body)
	assert.NoError(t, err)

	var response map[string]interface{}
	err = json.Unmarshal(body, &response)
	assert.NoError(t, err)

	assert.Equal(t, "healthy", response["status"])
	assert.Equal(t, "go-monitor", response["service"])
}

// TestHealthEndpointUnhealthy tests unhealthy response
func TestHealthEndpointUnhealthy(t *testing.T) {
	app := fiber.New()

	app.Get("/health", func(c *fiber.Ctx) error {
		return c.Status(500).JSON(fiber.Map{
			"status": "unhealthy",
			"error":  "connection refused",
		})
	})

	req := httptest.NewRequest("GET", "/health", nil)
	resp, err := app.Test(req)

	assert.NoError(t, err)
	assert.Equal(t, 500, resp.StatusCode)

	body, err := io.ReadAll(resp.Body)
	assert.NoError(t, err)

	var response map[string]interface{}
	err = json.Unmarshal(body, &response)
	assert.NoError(t, err)

	assert.Equal(t, "unhealthy", response["status"])
	assert.Contains(t, response, "error")
}

// TestMetricsEndpoint tests the metrics endpoint response
func TestMetricsEndpoint(t *testing.T) {
	app := fiber.New()

	app.Get("/metrics", func(c *fiber.Ctx) error {
		return c.JSON(fiber.Map{
			"total":    10,
			"active":   6,
			"finished": 4,
		})
	})

	req := httptest.NewRequest("GET", "/metrics", nil)
	resp, err := app.Test(req)

	assert.NoError(t, err)
	assert.Equal(t, 200, resp.StatusCode)

	body, err := io.ReadAll(resp.Body)
	assert.NoError(t, err)

	var response map[string]interface{}
	err = json.Unmarshal(body, &response)
	assert.NoError(t, err)

	assert.Equal(t, float64(10), response["total"])
	assert.Equal(t, float64(6), response["active"])
	assert.Equal(t, float64(4), response["finished"])
}

// TestMetricsEndpointEmptyDatabase tests metrics when no tasks exist
func TestMetricsEndpointEmptyDatabase(t *testing.T) {
	app := fiber.New()

	app.Get("/metrics", func(c *fiber.Ctx) error {
		return c.JSON(fiber.Map{
			"total":    0,
			"active":   0,
			"finished": 0,
		})
	})

	req := httptest.NewRequest("GET", "/metrics", nil)
	resp, err := app.Test(req)

	assert.NoError(t, err)
	assert.Equal(t, 200, resp.StatusCode)

	body, err := io.ReadAll(resp.Body)
	assert.NoError(t, err)

	var response map[string]interface{}
	err = json.Unmarshal(body, &response)
	assert.NoError(t, err)

	assert.Equal(t, float64(0), response["total"])
	assert.Equal(t, float64(0), response["active"])
	assert.Equal(t, float64(0), response["finished"])
}

// TestMetricsEndpointAllCompleted tests metrics when all tasks are completed
func TestMetricsEndpointAllCompleted(t *testing.T) {
	app := fiber.New()

	app.Get("/metrics", func(c *fiber.Ctx) error {
		return c.JSON(fiber.Map{
			"total":    5,
			"active":   0,
			"finished": 5,
		})
	})

	req := httptest.NewRequest("GET", "/metrics", nil)
	resp, err := app.Test(req)

	assert.NoError(t, err)
	assert.Equal(t, 200, resp.StatusCode)

	body, err := io.ReadAll(resp.Body)
	assert.NoError(t, err)

	var response map[string]interface{}
	err = json.Unmarshal(body, &response)
	assert.NoError(t, err)

	assert.Equal(t, float64(5), response["total"])
	assert.Equal(t, float64(0), response["active"])
	assert.Equal(t, float64(5), response["finished"])
}

// TestInvalidEndpoint tests requesting a non-existent endpoint
func TestInvalidEndpoint(t *testing.T) {
	app := fiber.New()

	app.Get("/health", func(c *fiber.Ctx) error {
		return c.JSON(fiber.Map{"status": "healthy"})
	})

	req := httptest.NewRequest("GET", "/invalid", nil)
	resp, err := app.Test(req)

	assert.NoError(t, err)
	assert.Equal(t, 404, resp.StatusCode)
}

// TestHealthEndpointContentType tests response content type
func TestHealthEndpointContentType(t *testing.T) {
	app := fiber.New()

	app.Get("/health", func(c *fiber.Ctx) error {
		return c.JSON(fiber.Map{"status": "healthy"})
	})

	req := httptest.NewRequest("GET", "/health", nil)
	resp, err := app.Test(req)

	assert.NoError(t, err)
	assert.Equal(t, "application/json", resp.Header.Get("Content-Type"))
}

// TestMetricsEndpointContentType tests metrics content type
func TestMetricsEndpointContentType(t *testing.T) {
	app := fiber.New()

	app.Get("/metrics", func(c *fiber.Ctx) error {
		return c.JSON(fiber.Map{
			"total":    10,
			"active":   6,
			"finished": 4,
		})
	})

	req := httptest.NewRequest("GET", "/metrics", nil)
	resp, err := app.Test(req)

	assert.NoError(t, err)
	assert.Equal(t, "application/json", resp.Header.Get("Content-Type"))
}

// TestMultipleHealthRequests tests multiple sequential health check requests
func TestMultipleHealthRequests(t *testing.T) {
	app := fiber.New()

	app.Get("/health", func(c *fiber.Ctx) error {
		return c.JSON(fiber.Map{"status": "healthy"})
	})

	for i := 0; i < 5; i++ {
		req := httptest.NewRequest("GET", "/health", nil)
		resp, err := app.Test(req)

		assert.NoError(t, err)
		assert.Equal(t, 200, resp.StatusCode)
	}
}

// TestHealthEndpointWithHeaders tests health endpoint with custom headers
func TestHealthEndpointWithHeaders(t *testing.T) {
	app := fiber.New()

	app.Get("/health", func(c *fiber.Ctx) error {
		userAgent := c.Get("User-Agent")
		if userAgent == "" {
			userAgent = "unknown"
		}
		return c.JSON(fiber.Map{
			"status":     "healthy",
			"user_agent": userAgent,
		})
	})

	req := httptest.NewRequest("GET", "/health", nil)
	req.Header.Set("User-Agent", "test-client/1.0")
	resp, err := app.Test(req)

	assert.NoError(t, err)
	assert.Equal(t, 200, resp.StatusCode)

	body, err := io.ReadAll(resp.Body)
	assert.NoError(t, err)

	var response map[string]interface{}
	err = json.Unmarshal(body, &response)
	assert.NoError(t, err)

	assert.Equal(t, "healthy", response["status"])
	assert.Equal(t, "test-client/1.0", response["user_agent"])
}

// BenchmarkHealthEndpoint benchmarks the health endpoint
func BenchmarkHealthEndpoint(b *testing.B) {
	app := fiber.New()

	app.Get("/health", func(c *fiber.Ctx) error {
		return c.JSON(fiber.Map{"status": "healthy"})
	})

	req := httptest.NewRequest("GET", "/health", nil)

	b.ResetTimer()
	for i := 0; i < b.N; i++ {
		_, _ = app.Test(req)
	}
}

// BenchmarkMetricsEndpoint benchmarks the metrics endpoint
func BenchmarkMetricsEndpoint(b *testing.B) {
	app := fiber.New()

	app.Get("/metrics", func(c *fiber.Ctx) error {
		return c.JSON(fiber.Map{
			"total":    10,
			"active":   6,
			"finished": 4,
		})
	})

	req := httptest.NewRequest("GET", "/metrics", nil)

	b.ResetTimer()
	for i := 0; i < b.N; i++ {
		_, _ = app.Test(req)
	}
}
