package org.example.tears.Advise;

import org.example.tears.Api.ApiException;
import org.example.tears.Api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AdviseController {

    // 🔥 Exception تبعك
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse> handleApiException(ApiException e) {

        return ResponseEntity.status(e.getStatus())
                .body(new ApiResponse(false, e.getMessage()));
    }

    // 🔥 Validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidation(MethodArgumentNotValidException e) {

        String msg = e.getFieldError().getDefaultMessage();

        return ResponseEntity.status(400)
                .body(new ApiResponse(false, msg));
    }

    // 🔥 أي خطأ ثاني
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGeneral(Exception e) {

        return ResponseEntity.status(500)
                .body(new ApiResponse(false, "❌ خطأ داخلي: " + e.getMessage()));
    }
}