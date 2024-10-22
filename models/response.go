// models/response.go
package models

type DefaultResponseDTO struct {
    Value string `json:"value"`
}

type ErrorResponse struct {
    Code  string `json:"code"`
    Error string `json:"error"`
}