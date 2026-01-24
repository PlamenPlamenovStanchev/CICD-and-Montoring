package main

import (
    "context"
    "fmt"
    "net/http"
	"database/sql"
    _ "github.com/go-sql-driver/mysql"
)

var ctx = context.Background()

func main() {
	dsn := fmt.Sprintf("%s:%s@tcp(%s:%d)/%s?parseTime=true",
		"app_user",
		"Parolka-12345",
		"localhost",
		3306,
		"counters",
	)

	db, err := sql.Open("mysql", dsn)
	if err != nil {
		fmt.Println("ERROR [main] cannot open DB: %v", err)
	}
	defer db.Close()

    http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
        if r.URL.Path != "/" {
            http.NotFound(w, r)
                return
        }	

		_, err := db.Exec("INSERT INTO hits () VALUES ();")
		if err != nil {
			fmt.Printf("ERROR [handler] insert error: %v", err)
			http.Error(w, "internal server error", http.StatusInternalServerError)
			return
		}

		var count int64
		err = db.QueryRow("SELECT COUNT(*) FROM hits;").Scan(&count)
		if err != nil {
			fmt.Printf("ERROR [handler] count error: %v", err)
			http.Error(w, "internal server error", http.StatusInternalServerError)
			return
		}

        fmt.Fprintf(w, "Hello! This Go app has been viewed %d times.\n", count)
    })

    fmt.Println("Go server starting on port 8080...")
    http.ListenAndServe(":8080", nil)
}
