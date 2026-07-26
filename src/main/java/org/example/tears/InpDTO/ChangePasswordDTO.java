package org.example.tears.InpDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChangePasswordDTO {

    @NotBlank
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$",
            message = "يجب أن تحتوي كلمة المرور على حرف كبير وصغير ورقم ورمز خاص، وألا تقل عن 8 أحرف"
    )
    private String newPassword;

    @NotBlank
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$",
            message = "يجب أن تحتوي كلمة المرور على حرف كبير وصغير ورقم ورمز خاص، وألا تقل عن 8 أحرف"
    )
    private String confirmPassword;
}
