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
    private AuthStatusDto user;

    public ApiResponse(boolean authenticated, AuthStatusDto user) {
        this.authenticated = authenticated;
        this.user = user;
    }

    // للنجاح برسالة فقط
    public ApiResponse(String message) {
        this.success = true;
        this.message = message;
    }

    // للنجاح برسالة + بيانات
    public ApiResponse(String message, Object data) {
        this.success = true;
        this.message = message;
        this.data = data;
    }

    // للنجاح برسالة + توكن (مثل تسجيل الدخول)
    public ApiResponse(String message, String token) {
        this.success = true;
        this.message = message;
        this.token = token;
    }

    // للخطأ برسالة فقط
    public static ApiResponse fail(String message) {
        ApiResponse res = new ApiResponse();
        res.success = false;
        res.message = message;
        return res;
    }

}
