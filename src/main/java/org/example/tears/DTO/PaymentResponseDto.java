package org.example.tears.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.tears.Enums.PaymentMethod;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDto {

    private Integer requestId;             // رقم الطلب
    private PaymentMethod paymentMethod;   // طريقة الدفع المستخدمة
    private String paymentTransactionId;   // معرف المعاملة أو الجلسة (Transaction ID)
    private boolean paid;                  // true إذا الدفع اكتمل مباشرة
    private boolean initialPaid;           // true إذا تم دفع الدفعة الأولى
    private boolean finalPaid;             // true إذا تم دفع المبلغ النهائي بعد التسعير
    private Integer amount;                // المبلغ الذي تم دفعه أو يجب دفعه
    private String errorMessage;           // رسالة الخطأ إذا فشل الدفع
}
