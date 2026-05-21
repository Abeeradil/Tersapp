package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Service
@RequiredArgsConstructor
public class NameService {

    public boolean isNameMatching(String userName, String ocrName) {

        if (userName == null || ocrName == null) return false;

        String n1 = normalizeText(userName);
        String n2 = normalizeText(ocrName);

        Set<String> u = new HashSet<>(Arrays.asList(n1.split("\\s+")));
        Set<String> o = new HashSet<>(Arrays.asList(n2.split("\\s+")));

        u.removeIf(p -> p.length() < 2);
        o.removeIf(p -> p.length() < 2);

        u.retainAll(o);

        return u.size() >= 2;
    }

    public String normalizeNameSmart(String name) {

        if (name == null) return null;

        name = name.trim().toLowerCase();

        Map<String, String> dict = Map.of(
                "ahmed", "احمد",
                "mohammed", "محمد",
                "ali", "علي",
                "abdullah", "عبدالله"
        );

        StringBuilder sb = new StringBuilder();

        for (String p : name.split("\\s+")) {
            sb.append(dict.getOrDefault(p, p)).append(" ");
        }

        return sb.toString().trim();
    }

    public String extractUserNameFromText(String text) {

        if (text == null) return null;

        text = normalizeText(text);

        String[] patterns = {
                "اسم المالك\\s*[:\\-]?\\s*([\\u0600-\\u06FF ]{3,40})",
                "المالك\\s*[:\\-]?\\s*([\\u0600-\\u06FF ]{3,40})",
                "المستخدم\\s*[:\\-]?\\s*([\\u0600-\\u06FF ]{3,40})",
                "اسم صاحب المركبة\\s*[:\\-]?\\s*([\\u0600-\\u06FF ]{3,40})"
        };

        for (String p : patterns) {
            Matcher m = Pattern.compile(p).matcher(text);
            if (m.find()) {
                return cleanName(m.group(1));
            }
        }

        return null;
    }

    public boolean isEnglish(String text) {
        return text != null &&
                text.matches(".*[a-zA-Z].*");
    }

    // ================= PRIVATE HELPERS =================

    private String cleanName(String name) {
        return name
                .replaceAll("[^\\u0600-\\u06FF ]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String normalizeText(String t) {
        return t.toLowerCase()
                .replace("أ", "ا")
                .replace("إ", "ا")
                .replace("آ", "ا")
                .replace("ة", "ه")
                .replace("ى", "ي")
                .replaceAll("[^\\u0600-\\u06FF a-z0-9]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}