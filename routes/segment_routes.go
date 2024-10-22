// routes/segment_routes.go
package routes

import (
    "encoding/json"
    "net/http"
    "github.com/gorilla/mux"
    "vk-segment-service/models"
)

var segments = make(map[int64]models.SegmentResponseDTO)
var nextSegmentID int64 = 1

func RegisterSegmentRoutes(r *mux.Router) {
    r.HandleFunc("/api/v1/segments", getSegments).Methods("GET")
    r.HandleFunc("/api/v1/segments", createSegment).Methods("POST")
    r.HandleFunc("/api/v1/segments/{id}", getSegment).Methods("GET")
    r.HandleFunc("/api/v1/segments/{id}", updateSegment).Methods("PUT")
    r.HandleFunc("/api/v1/segments/{id}", deleteSegment).Methods("DELETE")
}

func getSegments(w http.ResponseWriter, r *http.Request) {
    var response []models.SegmentResponseDTO
    for _, segment := range segments {
        response = append(response, segment)
    }
    respondWithJSON(w, http.StatusOK, response)
}

func createSegment(w http.ResponseWriter, r *http.Request) {
    var segment models.SegmentRequestDTO
    if err := json.NewDecoder(r.Body).Decode(&segment); err != nil {
        respondWithError(w, http.StatusBadRequest, "Invalid input data")
        return
    }

    newSegment := models.SegmentResponseDTO{
        SegmentID:   nextSegmentID,
        Name:        segment.Name,
        Description: segment.Description,
        CreatedAt:   time.Now(),
    }
    segments[nextSegmentID] = newSegment
    nextSegmentID++

    respondWithJSON(w, http.StatusCreated, newSegment)
}

func getSegment(w http.ResponseWriter, r *http.Request) {
    params := mux.Vars(r)
    id := params["id"]

    segment, exists := segments[id]
    if !exists {
        respondWithError(w, http.StatusNotFound, "Segment not found")
        return
    }

    respondWithJSON(w, http.StatusOK, segment)
}

func updateSegment(w http.ResponseWriter, r *http.Request) {
    params := mux.Vars(r)
    id := params["id"]

    var segment models.SegmentRequestDTO
    if err := json.NewDecoder(r.Body).Decode(&segment); err != nil {
        respondWithError(w, http.StatusBadRequest, "Invalid input data")
        return
    }

    existingSegment, exists := segments[id]
    if !exists {
        respondWithError(w, http.StatusNotFound, "Segment not found")
        return
    }

    existingSegment.Name = segment.Name
    existingSegment.Description = segment.Description
    segments[id] = existingSegment

    respondWithJSON(w, http.StatusOK, existingSegment)
}

func deleteSegment(w http.ResponseWriter, r *http.Request) {
    params := mux.Vars(r)
    id := params["id"]

    if _, exists := segments[id]; !exists {
        respondWithError(w, http.StatusNotFound, "Segment not found")
        return
    }

    delete(segments, id)
    respondWithJSON(w, http.StatusOK, models.DefaultResponseDTO{Value: "Segment deleted successfully"})
}

func respondWithJSON(w http.ResponseWriter, code int, payload interface{}) {
    response, _ := json.Marshal(payload)
    w.Header().Set("Content-Type", "application/json")
    w.WriteHeader(code)
    w.Write(response)
}

func respondWithError(w http.ResponseWriter, code int, message string) {
    respondWithJSON(w, code, models.ErrorResponse{Code: "error", Error: message})
}