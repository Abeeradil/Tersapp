package org.example.tears.Enums;

public enum WarrantyStatus {

    PENDING_REVIEW,          // طلب الضمان تم إنشاؤه وينتظر مراجعة خدمة العملاء

    APPROVED,                // تمت الموافقة على طلب الضمان

    REJECTED,                // تم رفض طلب الضمان

    RECEIVED,         // تمت الموافقة وننتظر استلام السيارة للضمان

    INSPECTION,              // الفني يفحص السيارة بسبب مشكلة الضمان

    REPAIRING,               // جاري إصلاح المشكلة

    TESTING,                 // تجربة السيارة بعد الإصلاح

    DELIVERY_IN_PROGRESS,    // الإصلاح انتهى والسيارة في مرحلة التسليم

    DELIVERED                // تم تسليم السيارة بعد إصلاح الضمان
}