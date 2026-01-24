package main

import (
    "context"
    "fmt"
    "net/http"
)

var ctx = context.Background()
var count = 0

func main() {
    http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
        count = count + 1
        fmt.Fprintf(w, "Hello! This Go app has been viewed %d times.\n", count)
    })

    fmt.Println("Go server starting on port 8080...")
    http.ListenAndServe(":8080", nil)
}
