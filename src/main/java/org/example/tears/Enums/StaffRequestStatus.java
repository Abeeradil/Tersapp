package org.example.tears.Enums;

public enum StaffRequestStatus {
        NEW,                    // جديد (وصل للمهندس بعد الدفع)
        RECEIVED,               // تم استلام السيارة
        INSPECTION_IN_PROGRESS, // جاري الفحص
        TESTING,                // قيد التجربة
        PARTS_REGISTERING,      // تسجيل القطع
        PRICING,                // جاري التسعير
        REPORT_WRITING,         // إعداد التقرير
        REPAIRING,              // جاري الإصلاح
        DELIVERY_IN_PROGRESS,   //جاري التسليم
        DELIVERED               // تم التسليم
}