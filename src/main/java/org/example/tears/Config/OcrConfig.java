package org.example.tears.Config;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OcrConfig {

    @Value("${tesseract.datapath}")
    private String dataPath;


    @Value("${tesseract.language}")
    private String language;

    @Bean
    public ITesseract tesseract() {

        Tesseract tesseract = new Tesseract();

        // 🔥 مسار ملفات اللغة
        tesseract.setDatapath(dataPath);

        // 🔥 اللغات
        tesseract.setLanguage(language);

        // 🔥 تحسين الدقة (مهم جدًا)
        tesseract.setPageSegMode(6);   // block of text
        tesseract.setOcrEngineMode(1); // LSTM mode (الأدق)

        // 🔥 تحسين OCR للصور الضعيفة
        tesseract.setTessVariable("user_defined_dpi", "300");
        tesseract.setTessVariable("preserve_interword_spaces", "1");

        return tesseract;
    }
}