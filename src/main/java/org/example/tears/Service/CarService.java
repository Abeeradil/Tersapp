package org.example.tears.Service;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.example.tears.Api.ApiException;
import org.example.tears.InpDTO.InpCarDto;
import org.example.tears.Model.*;
import org.example.tears.OutDTO.OutMyCarDTO;
import org.example.tears.Repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Service
@RequiredArgsConstructor
public class CarService {

        private static final Logger log = LoggerFactory.getLogger(CarService.class);

        private final CarRepository carRepository;
        private final AuthService authService;
        private final CarBrandRepository carBrandRepository;
        private final CarModelRepository carModelRepository;

        private final ITesseract tesseract;

        // ================= INIT OCR =================
        @PostConstruct
        public void init() {
            tesseract.setLanguage("ara+eng");
            tesseract.setPageSegMode(6);
            tesseract.setOcrEngineMode(1);

            // 🔥 تحسين الدقة
            tesseract.setVariable("user_defined_dpi", "300");
            tesseract.setVariable("preserve_interword_spaces", "1");
        }

        // =========================================================
        // AUTO REGISTER
        // =========================================================
        public Map<String, String> registerCarAuto(HttpServletRequest request,
                                                   MultipartFile formImage,
                                                   Integer mileage) {

            User user = authService.getAuthenticatedUser(request);

            Map<String, String> info = extractCarInfo(formImage);

            String extractedName = info.get("ownerName");

            if (extractedName == null)
                throw new ApiException("❌ لم يتم استخراج اسم المالك");

            // 🔥 المقارنة الأساسية
            if (!normalizeText(extractedName)
                    .equalsIgnoreCase(normalizeText(user.getFullName()))) {

                throw new ApiException("❌ اسم المالك في الاستمارة لا يطابق حسابك");
            }

            // إذا مطابق → نكمل التسجيل
            Car car = new Car();
            car.setCustomer(user.getCustomer());
            car.setPlateNumberArabic(info.get("plateNumberArabic"));
            car.setPlateNumberEnglish(convertPlateToEnglish(info.get("plateNumberArabic")));
            car.setCarYear(parseYear(info.get("carYear")));

            carRepository.save(car);

            return buildResponse(car, user.getFullName());
        }

        // =========================================================
        // OCR CORE (FIXED + STRONG)
        // =========================================================
        public Map<String, String> extractCarInfo(MultipartFile file) {

            try {
                BufferedImage original = ImageIO.read(file.getInputStream());
                if (original == null)
                    throw new ApiException("❌ الصورة غير صالحة");

                BufferedImage processed = enhanceImage(original);

                String text = tesseract.doOCR(processed);

                log.info("OCR TEXT => {}", text);

                Map<String, String> result = new LinkedHashMap<>();
                result.put("rawText", text);

                String name = extractUserNameFromText(text);
                if (name != null && !name.isBlank())
                    result.put("ownerName", name);

                Matcher plate = Pattern.compile("([\\u0621-\\u064A]{1,4}\\s*\\d{1,4})")
                        .matcher(text);

                if (plate.find())
                    result.put("plateNumberArabic", plate.group(1).trim());

                Matcher year = Pattern.compile("(19\\d{2}|20\\d{2})")
                        .matcher(text);

                if (year.find())
                    result.put("carYear", year.group());

                return result;

            } catch (Exception e) {
                throw new ApiException("❌ OCR Failed: " + e.getMessage());
            }
        }

        // =========================================================
        // IMAGE ENHANCEMENT 🔥 (IMPORTANT)
        // =========================================================
        private BufferedImage enhanceImage(BufferedImage image) {

            BufferedImage resized = new BufferedImage(
                    image.getWidth() * 2,
                    image.getHeight() * 2,
                    BufferedImage.TYPE_BYTE_GRAY
            );

            resized.getGraphics().drawImage(image, 0, 0,
                    image.getWidth() * 2,
                    image.getHeight() * 2,
                    null);

            return resized;
        }

        // =========================================================
        // NAME EXTRACTION
        // =========================================================
        public String extractUserNameFromText(String text) {

            if (text == null) return null;

            text = normalizeText(text);

            String[] patterns = {
                    "اسم المالك\\s*[:\\-]?\\s*([\\u0600-\\u06FF ]{3,40})",
                    "المالك\\s*[:\\-]?\\s*([\\u0600-\\u06FF ]{3,40})",
                    "اسم صاحب المركبة\\s*[:\\-]?\\s*([\\u0600-\\u06FF ]{3,40})"
            };

            for (String p : patterns) {
                Matcher m = Pattern.compile(p).matcher(text);
                if (m.find())
                    return cleanName(m.group(1));
            }

            return null;
        }

        private String normalizeText(String text) {
            return text.toLowerCase()
                    .replace("أ","ا")
                    .replace("إ","ا")
                    .replace("آ","ا")
                    .replace("ة","ه")
                    .replace("ى","ي")
                    .replaceAll("[^\\u0600-\\u06FF a-z0-9]", " ")
                    .replaceAll("\\s+", " ")
                    .trim();
        }

        private String cleanName(String name) {
            return name == null ? null :
                    name.replaceAll("[^\\u0600-\\u06FF ]", "")
                            .replaceAll("\\s+", " ")
                            .trim();
        }

        // =========================================================
        private Integer parseYear(String year) {
            try {
                return year != null ? Integer.parseInt(year) : null;
            } catch (Exception e) {
                return null;
            }
        }

        // =========================================================
        private Map<String, String> buildResponse(Car car, String ownerName) {

            Map<String, String> map = new LinkedHashMap<>();

            map.put("status", "✅ success");
            map.put("carId", car.getId().toString());
            map.put("ownerName", ownerName);
            map.put("plateArabic", car.getPlateNumberArabic());
            map.put("plateEnglish", car.getPlateNumberEnglish());

            return map;
        }


        /*--------------------------------------------------------------
            2) التسجيل اليدوي
        --------------------------------------------------------------*/
        public Map<String, String> registerCarManual(HttpServletRequest request, InpCarDto inpCarDto, MultipartFile formImage) {
            User user = authService.getAuthenticatedUser(request);

            Car car = buildCar(inpCarDto, formImage, user);
            carRepository.save(car);

            return buildResponse(car, user.getFullName());
        }
        public Map<String, String> extractUserNameFromAuthorization(MultipartFile authorizationDoc) {
            try {
                if (authorizationDoc == null || authorizationDoc.isEmpty())
                    throw new ApiException("❌ لم يتم رفع أي ملف للتفويض.");

                BufferedImage image = ImageIO.read(authorizationDoc.getInputStream());
                if (image == null) throw new ApiException("❌ الملف ليس صورة صالحة.");

                ITesseract tesseract = new Tesseract();
                tesseract.setLanguage("ara+eng");
                tesseract.setPageSegMode(6);
                tesseract.setOcrEngineMode(1);

                String content = tesseract.doOCR(image);
                log.info("Extracted OCR content: {}", content);

                // استخراج الاسم بعد كلمة "المستخدم"
                String userName = extractUserNameFromText(content);

                Map<String, String> nameParts = new HashMap<>();
                nameParts.put("fullName", userName);

                String[] parts = userName != null ? userName.split("\\s+") : new String[]{};
                nameParts.put("firstName", parts.length > 0 ? parts[0] : "");
                nameParts.put("middleName", parts.length > 2 ?
                        String.join(" ", Arrays.copyOfRange(parts, 1, parts.length - 1)) : "");
                nameParts.put("lastName", parts.length > 1 ? parts[parts.length - 1] : "");

                return nameParts;

            } catch (IOException | TesseractException e) {
                throw new ApiException("❌ فشل استخراج اسم المستخدم من التفويض: " + e.getMessage());
            }
        }


        /*--------------------------------------------------------------
            3) عرض سيارات المستخدم الحالي
        --------------------------------------------------------------*/
        public List<OutMyCarDTO> getMyCars(HttpServletRequest request) {
            User user = authService.getAuthenticatedUser(request);

            List<Car> cars = carRepository.findByCustomerId(user.getCustomer().getId());

            List<OutMyCarDTO> dtos = new ArrayList<>();
            for (Car car : cars) {
                OutMyCarDTO dto = new OutMyCarDTO();
                dto.setCarId(car.getId());
                dto.setPlateNumberArabic(car.getPlateNumberArabic());
                dto.setBrandNameAr(car.getBrand().getNameAr());
                dto.setModelNameAr(car.getModel().getNameAr());
                dto.setCarYear(car.getCarYear());
                dto.setCarImage(car.getModel().getImagePath() != null ? car.getModel().getImagePath() : "/carimage/default_car.png");
                dtos.add(dto);
            }
            return dtos;
        }

        /*--------------------------------------------------------------
            🧠 دوال مساعدة
        --------------------------------------------------------------*/
        private Car buildCar(InpCarDto inpCarDto, MultipartFile formImage, User user) {
            Car car = new Car();
            car.setCarYear(inpCarDto.getCarYear());
            car.setPlateNumberArabic(inpCarDto.getPlateNumberArabic());
            car.setPlateNumberEnglish(convertPlateToEnglish(inpCarDto.getPlateNumberArabic())); // توليد تلقائي
            car.setMileage(inpCarDto.getMileage());
            car.setCustomer(user.getCustomer());

            if (formImage != null && !formImage.isEmpty()) {
                car.setFormImagePath(saveFile(formImage, "forms"));
            }

            CarBrand brand = carBrandRepository.findById(inpCarDto.getBrandId())
                    .orElseThrow(() -> new ApiException("❌ البراند غير موجود"));

            CarModel model = carModelRepository.findById(inpCarDto.getModelId())
                    .orElseThrow(() -> new ApiException("❌ الموديل غير موجود"));

            car.setBrand(brand);
            car.setModel(model);

            return car;
        }


        private String saveFile(MultipartFile file, String folderName) {
            if (file == null || file.isEmpty()) return null;
            try {
                Path uploadPath = Paths.get("uploads/" + folderName);
                if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                return "/uploads/" + folderName + "/" + fileName;

            } catch (Exception e) {
                throw new ApiException("❌ فشل في حفظ الملف: " + e.getMessage());
            }
        }

        private CarBrand detectBrandFromText(String text) {
            String normalized = normalizeText(text);
            for (CarBrand brand : carBrandRepository.findAll()) {
                String ar = normalizeText(brand.getNameAr());
                String en = normalizeText(brand.getName());
                if (normalized.contains(ar) || normalized.contains(en)) {
                    log.info("Brand detected: {}", brand.getNameAr());
                    return brand;
                }
            }
            throw new ApiException("❌ لم يتم التعرف على ماركة السيارة");
        }

        private CarModel detectModelFromText(String text, CarBrand brand) {
            String normalized = normalizeText(text);
            return carModelRepository.findByBrandId(brand.getId()).stream()
                    .filter(m -> normalized.contains(normalizeText(m.getNameAr())) || normalized.contains(normalizeText(m.getName())))
                    .findFirst()
                    .orElseThrow(() -> new ApiException("❌ لم يتم التعرف على موديل السيارة"));
        }


        public String convertPlateToEnglish(String arabicPlate) {
            arabicPlate = arabicPlate.replace("أ","A").replace("ب","B").replace("ح","J")
                    .replace("د","D").replace("ر","R").replace("س","S")
                    .replace("ص","X").replace("ط","T").replace("ع","E")
                    .replace("ق","G").replace("ك","K").replace("ل","L")
                    .replace("م","Z").replace("ن","N").replace("ه","H")
                    .replace("و","U").replace("ي","V").replaceAll("\\s+","").trim();

            StringBuilder english = new StringBuilder();
            for(char ch : arabicPlate.toCharArray()) {
                if(Character.isDigit(ch)) english.append(ch);
                else if(ch == ' ') english.append(' ');
                else english.append(ch);
            }
            return english.toString();
        }

    public Map<String, String> extractOwnerName(MultipartFile formImage) {

        if (formImage == null || formImage.isEmpty())
            throw new ApiException("❌ يجب رفع صورة الاستمارة");

        try {
            BufferedImage image = ImageIO.read(formImage.getInputStream());

            if (image == null)
                throw new ApiException("❌ الصورة غير صالحة");

            BufferedImage processed = enhanceImage(image);

            String text = tesseract.doOCR(processed);

            log.info("OCR TEXT => {}", text);

            String ownerName = extractUserNameFromText(text);

            if (ownerName == null || ownerName.isBlank())
                throw new ApiException("❌ لم يتم العثور على اسم المالك");

            Map<String, String> response = new LinkedHashMap<>();
            response.put("ownerName", ownerName);
            response.put("rawText", text);

            return response;

        } catch (Exception e) {
            throw new ApiException("❌ فشل استخراج الاسم: " + e.getMessage());
        }
    }

    }
