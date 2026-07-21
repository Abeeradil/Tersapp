package org.example.tears.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.example.tears.Enums.TicketPriority;
import org.example.tears.Enums.TicketProblemType;

@Data
public class CreateTicketDto {

        @NotNull(message = "رقم الطلب مطلوب")
        private Integer requestId;

        @NotNull(message = "نوع المشكلة مطلوب")
        private TicketProblemType problemType;

        @NotNull(message = "الأولوية مطلوبة")
        private TicketPriority priority;

        @NotBlank(message = "الوصف مطلوب")
        @Size(max = 1000)
        private String description;
    }
