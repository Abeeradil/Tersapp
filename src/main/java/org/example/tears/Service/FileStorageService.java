package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

        public String saveFile(MultipartFile file, String folder) {

            try {
                if (file == null || file.isEmpty()) {
                    return null;
                }

                Path uploadPath = Paths.get("uploads/" + folder);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String original = file.getOriginalFilename();
                String extension = "";

                if (original != null && original.contains(".")) {
                    extension = original.substring(original.lastIndexOf("."));
                }

                String fileName = UUID.randomUUID() + extension;

                Path filePath = uploadPath.resolve(fileName);

                Files.copy(
                        file.getInputStream(),
                        filePath,
                        StandardCopyOption.REPLACE_EXISTING
                );

                return "http://localhost:8080/uploads/" + folder + "/" + fileName;

            } catch (Exception e) {
                throw new ApiException("❌ فشل حفظ الملف: " + e.getMessage());
            }
        }
    }