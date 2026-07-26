package org.example.tears.DTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ResetPasswordDto {
    @NotEmpty
    private String resetToken;
    @NotEmpty
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$",
            message = "يجب أن تحتوي كلمة المرور على حرف كبير وصغير ورقم ورمز خاص، وألا تقل عن 8 أحرف"
    )
    private String newPassword;

    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$",
            message = "يجب أن تحتوي كلمة المرور على حرف كبير وصغير ورقم ورمز خاص، وألا تقل عن 8 أحرف"
    )
    @NotEmpty
    private String confirmPassword;
}
