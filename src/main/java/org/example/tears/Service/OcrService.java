package org.example.tears.Service;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class OcrService {

    public String extractTextFromImage(String imagePath){
        try {
            ITesseract tesseract = new Tesseract();

            // لو اللغة عربية + إنجليزية (اختياري)
            // tesseract.setLanguage("ara+eng");

            return tesseract.doOCR(new File(imagePath));

        } catch (TesseractException e) {
            throw new RuntimeException("فشل استخراج النص من الصورة: " + e.getMessage());
        }
    }

    public String extractPhoneNumber(String text) {
        // يبحث عن رقم جوال سعودي بصيغة 05xxxxxxx أو 5xxxxxxxx
        String regex = "(05\\d{8}|5\\d{8})";
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(regex).matcher(text);
        return matcher.find() ? matcher.group() : null;
    }

}
