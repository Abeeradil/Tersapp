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



}
