package org.example.tears.Service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PlateService {

    private static final Map<String, String> AR_TO_EN = Map.ofEntries(
            Map.entry("أ", "A"),
            Map.entry("ب", "B"),
            Map.entry("ح", "J"),
            Map.entry("د", "D"),
            Map.entry("ر", "R"),
            Map.entry("س", "S"),
            Map.entry("ص", "X"),
            Map.entry("ط", "T"),
            Map.entry("ع", "E"),
            Map.entry("ق", "G"),
            Map.entry("ك", "K"),
            Map.entry("ل", "L"),
            Map.entry("م", "Z"),
            Map.entry("ن", "N"),
            Map.entry("ه", "H"),
            Map.entry("و", "U"),
            Map.entry("ى", "V")
    );

    private static final Map<String, String> EN_TO_AR = Map.ofEntries(
            Map.entry("A", "أ"),
            Map.entry("B", "ب"),
            Map.entry("J", "ح"),
            Map.entry("D", "د"),
            Map.entry("R", "ر"),
            Map.entry("S", "س"),
            Map.entry("X", "ص"),
            Map.entry("T", "ط"),
            Map.entry("E", "ع"),
            Map.entry("G", "ق"),
            Map.entry("K", "ك"),
            Map.entry("L", "ل"),
            Map.entry("Z", "م"),
            Map.entry("N", "ن"),
            Map.entry("H", "ه"),
            Map.entry("U", "و"),
            Map.entry("V", "ى")
    );

    // ================= NORMALIZE =================

    public String normalizePlate(String plate) {

        if (plate == null) {
            return null;
        }

        return plate
                .replaceAll("\\s+", " ")
                .trim()
                .toUpperCase();
    }

    // ================= CONVERT AR → EN =================

    public String convertPlateToEnglish(String arabic) {

        if (arabic == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        for (char c : arabic.toCharArray()) {

            String mapped =
                    AR_TO_EN.get(String.valueOf(c));

            sb.append(
                    mapped != null
                            ? mapped
                            : c
            );
        }

        return sb.toString().toUpperCase();
    }

    // ================= CONVERT EN → AR =================

    public String convertPlateToArabic(String english) {

        if (english == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        for (char c : english.toUpperCase().toCharArray()) {

            String mapped =
                    EN_TO_AR.get(String.valueOf(c));

            sb.append(
                    mapped != null
                            ? mapped
                            : c
            );
        }

        return sb.toString();
    }

    // ================= VALIDATE AR =================

    public void validatePlate(String plate) {

        if (plate == null || plate.isBlank()) {
            throw new RuntimeException(
                    "Invalid Arabic plate"
            );
        }
    }

    // ================= VALIDATE EN =================

    public void validateEnglishPlate(String plate) {

        if (plate == null || plate.isBlank()) {
            throw new RuntimeException(
                    "Invalid English plate"
            );
        }
    }

    // ================= NORMALIZE BOTH =================

    public Map<String, String> normalizePlatePair(
            String arabic,
            String english
    ) {

        String ar = normalizePlate(arabic);
        String en = normalizePlate(english);

        /*
         * إذا العربي موجود والإنجليزي غير موجود
         * نحوله من العربي.
         */
        if ((en == null || en.isBlank())
                && ar != null
                && !ar.isBlank()) {

            en = convertPlateToEnglish(ar);
        }

        /*
         * إذا الإنجليزي موجود والعربي غير موجود
         * نحوله من الإنجليزي.
         */
        if ((ar == null || ar.isBlank())
                && en != null
                && !en.isBlank()) {

            ar = convertPlateToArabic(en);
        }

        /*
         * إذا الاثنين موجودين:
         *
         * لا نخمن أي حرف ناقص.
         * نحتفظ بما قرأه OpenAI.
         */

        return Map.of(
                "ar", ar,
                "en", en
        );
    }
}