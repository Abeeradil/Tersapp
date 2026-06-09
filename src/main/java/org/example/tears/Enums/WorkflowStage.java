package org.example.tears.Enums;

public enum WorkflowStage {

        NEW_REQUEST,            // تم إنشاء الطلب (قبل الإسناد)
        ASSIGNED,               // تم إسناد الطلب لموظف
        RECEIVED,               // تم استلام السيارة من العميل
        INSPECTION_IN_PROGRESS, // جاري فحص السيارة
        TESTING,                // تجربة/تشغيل بعد الفحص (إذا احتجتها)
        REPORT_WRITING,         // كتابة تقرير الحالة
        PARTS_REGISTERING,      // تسجيل القطع المطلوبة
        PRICING,                // تسعير الإصلاح
        WAITING_APPROVAL,       // انتظار موافقة العميل على السعر
        REPAIRING,              // بدء الإصلاح الفعلي
        READY,                  // جاهز للتسليم
        DELIVERED,              // تم تسليم السيارة
        CANCELLED               // تم إلغاء الطلب
}
