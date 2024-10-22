// main.go
package main

import (
    "log"
    "net/http"
    "github.com/gorilla/mux"
    "vk-segment-service/routes"
)

func main() {
    r := mux.NewRouter()

    // Initialize routes
    routes.RegisterSegmentRoutes(r)

    // Start server
    log.Println("Server is running on port 8090")
    if err := http.ListenAndServe(":8090", r); err != nil {
        log.Fatal(err)
    }
}