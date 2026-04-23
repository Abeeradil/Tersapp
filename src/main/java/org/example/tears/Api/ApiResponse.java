package org.example.tears.Api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.tears.OutDTO.AuthStatusDto;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class ApiResponse {

    private boolean success;
    private String message;
    private Object data;
    private String token;

    public ApiResponse(boolean success, Object data) {
        this.success = success;
        this.data = data;
    }

    public ApiResponse(String message) {
        this.success = true;
        this.message = message;
    }

    public ApiResponse(String message, String token) {
        this.success = true;
        this.message = message;
        this.token = token;
    }
}