package org.example.tears.Enums;

public enum StaffRequestStatus {
        NEW,                    // جديد (وصل للمهندس بعد الدفع)
        RECEIVED,               // تم استلام السيارة
        INSPECTION_IN_PROGRESS, // جاري الفحص
        TESTING,                // قيد التجربة
        REPORT_WRITING,         // إعداد التقرير
        PARTS_REGISTERING,      // تسجيل القطع
        PRICING,                // جاري التسعير
        REPAIRING,              // جاري الإصلاح
        READY,                  // جاهز للتسليم
        DELIVERED               // تم التسليم

}
