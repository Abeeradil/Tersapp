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
    private boolean authenticated;
    private Object user;

    // success + data
    public ApiResponse(boolean success, Object data) {
        this.success = success;
        this.data = data;

        if (data instanceof AuthStatusDto auth) {
            this.authenticated = auth.isAuthenticated();
            this.user = auth;
        }
    }

    // message فقط
    public ApiResponse(String message) {
        this.success = true;
        this.message = message;
    }

    // message + token
    public ApiResponse(String message, String token) {
        this.success = true;
        this.message = message;
        this.token = token;
        this.authenticated = true;
    }
}