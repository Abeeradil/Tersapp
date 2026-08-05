package org.example.tears.Enums;

public enum StaffRequestActionRule {

        RECEIVED(true, false), // لازم صورة
        INSPECTION(false, false),
        REPAIR(false, false),
        COMPLETED(false, false);

        private final boolean requiresImage;
        private final boolean requiresNote;

        StaffRequestActionRule(boolean requiresImage, boolean requiresNote) {
            this.requiresImage = requiresImage;
            this.requiresNote = requiresNote;
        }


        public boolean isRequiresNote() {
            return requiresNote;
        }

    public static StaffRequestActionRule fromStatus(StaffRequestStatus status) {
        return switch (status) {
            case RECEIVED -> StaffRequestActionRule.RECEIVED;
            default -> StaffRequestActionRule.INSPECTION;
        };
    }

}
