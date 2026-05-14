package org.example.tears.Service;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.sourceforge.tess4j.ITesseract;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.example.tears.Api.ApiException;
import org.example.tears.InpDTO.InpCarDto;
import org.example.tears.Model.*;
import org.example.tears.OutDTO.OutMyCarDTO;
import org.example.tears.Repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CarService {

    private static final Logger log = LoggerFactory.getLogger(CarService.class);

    // ================= DEPENDENCIES =================
    private final CarRepository carRepository;
    private final AuthService authService;
    private final CarBrandRepository carBrandRepository;
    private final CarModelRepository carModelRepository;
    private final ITesseract tesseract;

    // =========================================================
    // LETTER MAPS
    // =========================================================
    private static final Map<String, String> AR_TO_EN = Map.ofEntries(
            Map.entry("أ", "A"), Map.entry("ب", "B"), Map.entry("ح", "J"),
            Map.entry("د", "D"), Map.entry("ر", "R"), Map.entry("س", "S"),
            Map.entry("ص", "X"), Map.entry("ط", "T"), Map.entry("ع", "E"),
            Map.entry("ق", "G"), Map.entry("ك", "K"), Map.entry("ل", "L"),
            Map.entry("م", "Z"), Map.entry("ن", "N"), Map.entry("ه", "H"),
            Map.entry("و", "U"), Map.entry("ى", "V")
    );

    private static final Map<String, String> EN_TO_AR = Map.ofEntries(
            Map.entry("A", "أ"), Map.entry("B", "ب"), Map.entry("J", "ح"),
            Map.entry("D", "د"), Map.entry("R", "ر"), Map.entry("S", "س"),
            Map.entry("X", "ص"), Map.entry("T", "ط"), Map.entry("E", "ع"),
            Map.entry("G", "ق"), Map.entry("K", "ك"), Map.entry("L", "ل"),
            Map.entry("Z", "م"), Map.entry("N", "ن"), Map.entry("H", "ه"),
            Map.entry("U", "و"), Map.entry("V", "ى")
    );
    public Map<String, Object> registerCarManual(
            HttpServletRequest request,
            InpCarDto inpCarDto,
            MultipartFile formImage
    ) {

        User user = authService.getAuthenticatedUser(request);

        // ================= VALIDATION =================
        if (inpCarDto == null) {
            throw new ApiException("❌ البيانات مطلوبة");
        }

        if (inpCarDto.getBrandId() == null) {
            throw new ApiException("❌ البراند مطلوب");
        }

        if (inpCarDto.getModelId() == null) {
            throw new ApiException("❌ الموديل مطلوب");
        }

        // ================= CHECK BRAND =================
        CarBrand brand = carBrandRepository.findById(inpCarDto.getBrandId())
                .orElseThrow(() -> new ApiException("❌ البراند غير موجود"));

        // ================= CHECK MODEL =================
        CarModel model = carModelRepository.findById(inpCarDto.getModelId())
                .orElseThrow(() -> new ApiException("❌ الموديل غير موجود"));

        // ================= CHECK RELATION =================
        if (!model.getBrand().getId().equals(brand.getId())) {
            throw new ApiException("❌ الموديل لا يتبع لهذا البراند");
        }

        // ================= IMAGE VALIDATION =================
        if (formImage == null || formImage.isEmpty()) {
            throw new ApiException("❌ صورة الاستمارة مطلوبة");
        }

        // ================= PREVENT DUPLICATE =================
        if (inpCarDto.getPlateNumberArabic() != null &&
                carRepository.existsByPlateNumberArabic(inpCarDto.getPlateNumberArabic())) {
            throw new ApiException("❌ السيارة مسجلة مسبقًا");
        }

        // ================= BUILD CAR =================
        Car car = buildCar(inpCarDto, formImage, user);

        car.setBrand(brand);
        car.setModel(model);

        // ================= SAVE =================
        carRepository.save(car);

        return buildResponse(car, user.getFullName());
    }

    private Car buildCar(
            InpCarDto inpCarDto,
            MultipartFile formImage,
            User user
    ) {

        Car car = new Car();

        car.setCarYear(inpCarDto.getCarYear());

        String arabicPlate =
                normalizePlate(inpCarDto.getPlateNumberArabic());

        String englishPlate =
                normalizePlate(inpCarDto.getPlateNumberEnglish());

        if (arabicPlate != null && !arabicPlate.isBlank()) {

            validatePlate(arabicPlate);

            if (englishPlate == null || englishPlate.isBlank()) {
                englishPlate = convertPlateToEnglish(arabicPlate);
            }

        } else if (englishPlate != null && !englishPlate.isBlank()) {

            validateEnglishPlate(englishPlate);

            arabicPlate = convertPlateToArabic(englishPlate);

        } else {
            throw new ApiException("❌ رقم اللوحة مطلوب");
        }

        car.setPlateNumberArabic(arabicPlate);
        car.setPlateNumberEnglish(englishPlate);

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

    public List<OutMyCarDTO> getMyCars(HttpServletRequest request) {

        User user = authService.getAuthenticatedUser(request);

        List<Car> cars = carRepository.findByCustomerId(user.getCustomer().getId());

        List<OutMyCarDTO> result = new ArrayList<>();

        for (Car car : cars) {

            OutMyCarDTO dto = new OutMyCarDTO();

            dto.setCarId(car.getId());
            dto.setPlateNumberArabic(car.getPlateNumberArabic());
            dto.setBrandNameAr(car.getBrand().getNameAr());
            dto.setModelNameAr(car.getModel().getNameAr());
            dto.setCarYear(car.getCarYear());

            dto.setCarImage(
                    car.getModel().getImagePath() != null
                            ? car.getModel().getImagePath()
                            : "/carimage/default_car.png"
            );

            result.add(dto);
        }

        return result;
    }

    private String saveFile(MultipartFile file, String folder) {

        try {

            if (file == null || file.isEmpty())
                return null;

            Path uploadPath = Paths.get("uploads/" + folder);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            Path filePath = uploadPath.resolve(fileName);

            Files.copy(
                    file.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return "/uploads/" + folder + "/" + fileName;

        } catch (Exception e) {
            throw new ApiException("❌ فشل حفظ الملف: " + e.getMessage());
        }
    }

    public Map<String, String> extractOwnerName(MultipartFile formImage) {

        Map<String, String> result = extractCarInfo(formImage);

        Map<String, String> response = new LinkedHashMap<>();

        response.put("ownerName", result.get("ownerName"));
        response.put("rawText", result.get("rawText"));

        return response;
    }
    // ================= INIT OCR =================
    @PostConstruct
    public void init() {
        tesseract.setLanguage("ara+eng");
        tesseract.setPageSegMode(6);
        tesseract.setOcrEngineMode(1);
        tesseract.setTessVariable("user_defined_dpi", "300");
        tesseract.setTessVariable("preserve_interword_spaces", "1");
    }

    // =========================================================
    // AUTO REGISTER
    // =========================================================
//    public Map<String, Object> registerCarAuto(
//            HttpServletRequest request,
//            MultipartFile formImage,
//            Integer mileage
//    ) {
//
//        User user = authService.getAuthenticatedUser(request);
//
//        if (formImage == null || formImage.isEmpty())
//            throw new ApiException("❌ يجب رفع صورة الاستمارة");
//
//        Map<String, String> info = extractCarInfo(formImage);
//
//        String extractedName = info.get("ownerName");
//        String userName = user.getFullName();
//
//        if (isEnglish(extractedName))
//            extractedName = normalizeNameSmart(extractedName);
//
//        if (!isNameMatching(userName, extractedName))
//            throw new ApiException("❌ اسم صاحب الاستمارة لا يطابق حسابك");
//
//        String rawText = info.get("rawText");
//        if (rawText == null || rawText.length() < 10)
//            throw new ApiException("❌ الصورة غير واضحة");
//
//        String plate = normalizePlate(info.get("plateNumberArabic"));
//        if (plate == null || plate.isBlank())
//            throw new ApiException("❌ لم يتم استخراج رقم اللوحة");
//
//        validatePlate(plate);
//
//        if (carRepository.existsByPlateNumberArabic(plate))
//            throw new ApiException("❌ هذه اللوحة مسجلة مسبقًا");
//
//        CarBrand brand = detectBrandFromText(rawText);
//        CarModel model = detectModelFromText(rawText, brand);
//
//        Car car = new Car();
//        car.setCustomer(user.getCustomer());
//        car.setPlateNumberArabic(plate);
//        car.setPlateNumberEnglish(convertPlateToEnglish(plate));
//        car.setBrand(brand);
//        car.setModel(model);
//        car.setMileage(mileage);
//        car.setCarYear(parseYear(info.get("carYear")));
//
//        // ================= DEV LOG =================
//        log.info("========== OCR RAW TEXT ==========\n{}", rawText);
//        log.info("[DEV] OWNER OCR => {}", extractedName);
//        log.info("[DEV] USER NAME => {}", userName);
//        log.info("[DEV] BRAND => {}", brand.getNameAr());
//        log.info("[DEV] MODEL => {}", model.getNameAr());
//        log.info("[DEV] PLATE AR => {}", car.getPlateNumberArabic());
//        log.info("[DEV] PLATE EN => {}", car.getPlateNumberEnglish());
//
//        carRepository.save(car);
//
//        return buildResponse(car, user.getFullName());
//    }

    public Map<String, Object> registerCarAuto(
            HttpServletRequest request,
            MultipartFile formImage,
            Integer mileage
    ) {

        User user = authService.getAuthenticatedUser(request);

        if (formImage == null || formImage.isEmpty())
            throw new ApiException("❌ يجب رفع صورة الاستمارة");

        Map<String, String> info = extractCarInfo(formImage);

        String rawText = info.get("rawText");
        if (rawText == null || rawText.length() < 10)
            throw new ApiException("❌ الصورة غير واضحة");

        // ================= PLATE =================
        String plate = extractPlateSmart(rawText);

        if (plate == null || plate.length() < 3) {
            throw new ApiException("❌ لم يتم استخراج رقم اللوحة بشكل صحيح");
        }

        plate = normalizePlate(plate);

        if (plate == null || plate.split(" ").length < 2) {
            throw new ApiException("❌ لم يتم استخراج اللوحة بشكل صحيح");
        }

        if (plate == null || plate.replaceAll("\\s+", "").length() < 3) {
            throw new ApiException("❌ اللوحة غير واضحة");
        }


        if (carRepository.existsByPlateNumberArabic(plate))
            throw new ApiException("❌ هذه اللوحة مسجلة مسبقًا");

        // ================= BRAND / MODEL =================
        CarBrand brand = detectBrandFromText(rawText);
        CarModel model = detectModelFromText(rawText, brand);

        // ================= OPTIONAL OWNER CHECK =================
        String extractedName = info.get("ownerName");

        if (extractedName != null && !extractedName.isBlank()) {

            String userName = user.getFullName();

            if (isEnglish(extractedName))
                extractedName = normalizeNameSmart(extractedName);

            boolean match = isNameMatching(userName, extractedName);

            if (!match) {
                log.warn("⚠️ Owner mismatch (ignored) OCR='{}' USER='{}'",
                        extractedName, userName);
            } else {
                log.info("✅ Owner matched: {}", extractedName);
            }
        }

        // ================= SAVE CAR =================
        Car car = new Car();
        car.setCustomer(user.getCustomer());
        car.setPlateNumberArabic(plate);
        car.setPlateNumberEnglish(convertPlateToEnglish(plate));
        car.setBrand(brand);
        car.setModel(model);
        car.setMileage(mileage);
        car.setCarYear(parseYear(info.get("carYear")));

        carRepository.save(car);

        return buildResponse(car, user.getFullName());
    }

    // =========================================================
    // OCR
    // =========================================================
    public Map<String, String> extractCarInfo(MultipartFile file) {

        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null)
                throw new ApiException("❌ الصورة غير صالحة");

            BufferedImage processed = enhanceImage(image);
            String text = tesseract.doOCR(processed);

            Map<String, String> result = new LinkedHashMap<>();
            result.put("rawText", text);

            log.info("\n========== OCR RAW ==========\n{}\n=============================", text);

            String name = extractUserNameFromText(text);
            if (name != null)
                result.put("ownerName", name);

            String plate = extractPlateFromRawText(text);
            if (plate != null) {
                result.put("plateNumberArabic", plate);
            }

            Matcher year = Pattern.compile("(19\\d{2}|20\\d{2})").matcher(text);
            if (year.find())
                result.put("carYear", year.group());

            log.info("OCR CLEAN TEXT => \n{}", text);

            return result;

        } catch (Exception e) {
            throw new ApiException("❌ OCR Failed: " + e.getMessage());
        }
    }

    // =========================================================
    // NAME MATCHING
    // =========================================================
    private boolean isNameMatching(String userName, String ocrName) {
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

    private String extractPlateFromRawText(String text) {

        if (text == null) return null;

        text = text.replaceAll("[^\\p{L}\\p{N}\\s]", " ");

        Pattern pattern = Pattern.compile("([A-Z]{1,3}\\s?[0-9]{1,4})");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }

    private String extractPlateSmart(String text) {

        if (text == null) return null;

        text = text.replaceAll("[^A-Za-z0-9\\u0600-\\u06FF ]", " ");
        text = text.replaceAll("\\s+", " ").trim();

        // نجمع أي حروف متفرقة قبل الرقم
        Matcher m = Pattern.compile("([A-Za-z\\u0600-\\u06FF\\s]{1,10})(\\d{2,5})").matcher(text);

        if (m.find()) {
            String letters = m.group(1).replaceAll("\\s+", "");
            String numbers = m.group(2);

            if (letters.length() >= 1 && numbers.length() >= 2) {
                return letters + " " + numbers;
            }
        }

        return null;
    }

    // =========================================================
    // NAME NORMALIZATION
    // =========================================================
    private String normalizeNameSmart(String name) {
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

    private CarBrand detectBrandFromText(String text) {

        String normalized = normalizeText(text);

        log.info("OCR NORMALIZED TEXT => {}", normalized);

        LevenshteinDistance distance = new LevenshteinDistance();

        return carBrandRepository.findAll()
                .stream()
                .min(Comparator.comparingInt(brand -> {

                    String ar = normalizeText(brand.getNameAr());
                    String en = normalizeText(brand.getName());

                    int arDistance = distance.apply(normalized, ar);
                    int enDistance = distance.apply(normalized, en);

                    return Math.min(arDistance, enDistance);
                }))
                .orElseThrow(() -> new ApiException("❌ لم يتم التعرف على الماركة"));
    }


    private double similarity(String s1, String s2) {

        s1 = normalizeText(s1);
        s2 = normalizeText(s2);

        if (s1.contains(s2) || s2.contains(s1)) {
            return 1.0;
        }

        Set<String> a = new HashSet<>(Arrays.asList(s1.split(" ")));
        Set<String> b = new HashSet<>(Arrays.asList(s2.split(" ")));

        Set<String> inter = new HashSet<>(a);
        inter.retainAll(b);

        return (double) inter.size() / Math.max(a.size(), b.size());
    }

    private CarModel detectModelFromText(String text, CarBrand brand) {

        String normalized = normalizeText(text);

        return carModelRepository.findByBrandId(brand.getId())
                .stream()
                .map(model -> new AbstractMap.SimpleEntry<>(
                        model,
                        similarity(normalized, normalizeText(model.getNameAr()))
                ))
                .filter(entry -> entry.getValue() > 0.4)
                .max(Comparator.comparingDouble(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new ApiException("❌ لم يتم التعرف على الموديل"));
    }


    // =========================================================
    // PLATE CONVERSION
    // =========================================================
    public String convertPlateToEnglish(String ar) {
        if (ar == null) return null;

        String r = ar;
        for (var e : AR_TO_EN.entrySet())
            r = r.replace(e.getKey(), e.getValue());

        return r.replaceAll("\\s+", "").toUpperCase();
    }

    public String convertPlateToArabic(String en) {
        if (en == null) return null;

        String r = en.toUpperCase();
        for (var e : EN_TO_AR.entrySet())
            r = r.replace(e.getKey(), e.getValue());

        return r;
    }

    // =========================================================
    // VALIDATION
    // =========================================================
    private void validatePlate(String plate) {

        if (plate == null || plate.length() < 3)
            throw new ApiException("❌ صيغة اللوحة غير صحيحة");

        // تقليل الصرامة لأن OCR مو دقيق
        if (!plate.matches(".*\\d+.*"))
            throw new ApiException("❌ اللوحة غير واضحة");
    }

    private void validateEnglishPlate(String plate) {

        if (plate == null || plate.isBlank())
            throw new ApiException("❌ English plate required");

        boolean valid = plate.matches("^\\d{1,4}\\s?[A-Z]{1,3}$");

        if (!valid)
            throw new ApiException("❌ Invalid English plate format");
    }

    // =========================================================
    // HELPERS
    // =========================================================
    private BufferedImage enhanceImage(BufferedImage img) {
        BufferedImage gray = new BufferedImage(
                img.getWidth(),
                img.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY
        );
        gray.getGraphics().drawImage(img, 0, 0, null);
        return gray;
    }
    private boolean isEnglish(String text) {
        return text != null && text.chars().anyMatch(Character::isLetter)
                && text.matches(".*[a-zA-Z].*");
    }

    private Integer parseYear(String y) {
        try { return y == null ? null : Integer.parseInt(y); }
        catch (Exception e) { return null; }
    }

    private String normalizePlate(String p) {

        if (p == null)
            return null;

        return p.trim().replaceAll("\\s+", " ");
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

    private String cleanName(String name) {

        if (name == null) return null;

        return name
                .replaceAll("[^\\u0600-\\u06FF ]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    // =========================================================
    // RESPONSE
    // =========================================================
    private Map<String, Object> buildResponse(Car car, String owner) {

        Map<String, Object> m = new LinkedHashMap<>();

        m.put("status", "success");
        m.put("carId", car.getId());

        m.put("ownerName", owner);

        m.put("brandName", car.getBrand().getName());
        m.put("brandNameAr", car.getBrand().getNameAr());

        m.put("modelName", car.getModel().getName());
        m.put("modelNameAr", car.getModel().getNameAr());

        m.put("plateArabic", car.getPlateNumberArabic());
        m.put("plateEnglish", car.getPlateNumberEnglish());

        m.put(
                "carImage",
                car.getModel().getImagePath() != null
                        ? car.getModel().getImagePath()
                        : "/carimage/default_car.png"
        );

        return m;
    }
    public Map<String, Object> updateCar(
            HttpServletRequest request,
            Integer carId,
            InpCarDto dto
    ) {

        User user = authService.getAuthenticatedUser(request);

        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ApiException("❌ السيارة غير موجودة"));

        // 🔴 تأكد أنها ملك المستخدم
        if (!car.getCustomer().getId().equals(user.getCustomer().getId())) {
            throw new ApiException("❌ غير مسموح تعديل سيارة ليست لك");
        }

        // ===== تحديث البيانات =====
        if (dto.getCarYear() != null)
            car.setCarYear(dto.getCarYear());

        if (dto.getMileage() != null)
            car.setMileage(dto.getMileage());

        if (dto.getBrandId() != null) {
            CarBrand brand = carBrandRepository.findById(dto.getBrandId())
                    .orElseThrow(() -> new ApiException("❌ البراند غير موجود"));
            car.setBrand(brand);
        }

        if (dto.getModelId() != null) {
            CarModel model = carModelRepository.findById(dto.getModelId())
                    .orElseThrow(() -> new ApiException("❌ الموديل غير موجود"));
            car.setModel(model);
        }

        // plate update (اختياري)
        if (dto.getPlateNumberArabic() != null || dto.getPlateNumberEnglish() != null) {

            String ar = normalizePlate(dto.getPlateNumberArabic());
            String en = normalizePlate(dto.getPlateNumberEnglish());

            if (ar != null) {
                validatePlate(ar);
                car.setPlateNumberArabic(ar);
                car.setPlateNumberEnglish(convertPlateToEnglish(ar));
            }

            if (en != null) {
                validateEnglishPlate(en);
                car.setPlateNumberEnglish(en);
                car.setPlateNumberArabic(convertPlateToArabic(en));
            }
        }

        carRepository.save(car);

        return buildResponse(car, user.getFullName());
    }
    public void deleteCar(HttpServletRequest request, Integer carId) {

        User user = authService.getAuthenticatedUser(request);

        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ApiException("❌ السيارة غير موجودة"));

        if (!car.getCustomer().getId().equals(user.getCustomer().getId())) {
            throw new ApiException("❌ غير مسموح حذف سيارة ليست لك");
        }

        car.setDeleted(true);
        carRepository.save(car);
    }
}