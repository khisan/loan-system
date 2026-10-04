package com.loan.loan_system.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Biar field yang null tidak muncul di response JSON
public class ApiResponse<T> {
  private boolean success;
  private String message;
  private T data;

  // Helper method untuk membuat response sukses
  public static <T> ApiResponse<T> success(String message, T data) {
    return ApiResponse.<T>builder()
    .success(true)
    .message(message)
    .data(data)
    .build();
  }

  // Helper method untuk membuat response gagal
  public static <T> ApiResponse<T> failure(String message) {
    return ApiResponse.<T>builder()
    .success(false)
    .message(message)
    .data(null)
    .build();
  } 
}
