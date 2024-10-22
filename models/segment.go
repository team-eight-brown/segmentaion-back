// models/segment.go
package models

import "time"

type SegmentRequestDTO struct {
    Name        string `json:"name"`
    Description string `json:"description"`
}

type SegmentResponseDTO struct {
    SegmentID   int64     `json:"segmentId"`
    Name        string    `json:"name"`
    Description string    `json:"description"`
    CreatedAt   time.Time `json:"createdAt"`
}