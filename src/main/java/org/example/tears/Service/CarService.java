package org.example.tears.Service;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.sourceforge.tess4j.ITesseract;
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
    public Map<String, String> registerCarAuto(HttpServletRequest request, MultipartFile formImage, Integer mileage) {

        User user = authService.getAuthenticatedUser(request);

        if (formImage == null || formImage.isEmpty())
            throw new ApiException("❌ يجب رفع صورة الاستمارة");
        Map<String, String> info = extractCarInfo(formImage);

        String extractedName = info.get("ownerName");
        String userName = user.getFullName();

        if (isEnglish(extractedName)) {
            extractedName = convertNameToArabic(extractedName);
        }

        if (!isNameMatching(userName, extractedName)) {
            throw new ApiException("❌ اسم صاحب الاستمارة لا يطابق حسابك");
        }



        String rawText = info.get("rawText");
        if (rawText == null || rawText.trim().length() < 10)
            throw new ApiException("❌ الصورة غير واضحة");

        String plate = normalizePlate(info.get("plateNumberArabic"));

        if (plate == null || plate.isBlank())
            throw new ApiException("❌ لم يتم استخراج رقم اللوحة");

        if (carRepository.existsByPlateNumberArabic(plate))
            throw new ApiException("❌ هذه اللوحة مسجلة مسبقًا");

        CarBrand brand = detectBrandFromText(rawText);
        CarModel model = detectModelFromText(rawText, brand);

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
    // NAME MATCHING (SMART 🔥)
// =========================================================
    private boolean isNameMatching(String userName, String ocrName) {

        if (userName == null || ocrName == null) return false;

        String n1 = normalizeText(userName);
        String n2 = normalizeText(ocrName);

        // تقسيم الأسماء
        Set<String> userParts = new HashSet<>(Arrays.asList(n1.split(" ")));
        Set<String> ocrParts = new HashSet<>(Arrays.asList(n2.split(" ")));

        // نحذف الكلمات الصغيرة
        userParts.removeIf(p -> p.length() < 2);
        ocrParts.removeIf(p -> p.length() < 2);

        // نحسب التقاطع
        userParts.retainAll(ocrParts);

        // شرط النجاح (ممكن تعدليه)
        return userParts.size() >= 2;
    }

    // =========================================================
    // OCR CORE
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
            if (name != null) result.put("ownerName", name);

            Matcher plate = Pattern.compile("([\\u0621-\\u064A]{1,3}\\s*\\d{1,4})").matcher(text);
            if (plate.find())
                result.put("plateNumberArabic", plate.group(1).trim());

            Matcher year = Pattern.compile("(19\\d{2}|20\\d{2})").matcher(text);
            if (year.find())
                result.put("carYear", year.group());

            return result;

        } catch (Exception e) {
            throw new ApiException("❌ OCR Failed: " + e.getMessage());
        }
    }

    // =========================================================
    // EXTRACT OWNER NAME ONLY (ENDPOINT)
    // =========================================================
    public Map<String, String> extractOwnerName(MultipartFile formImage) {

        if (formImage == null || formImage.isEmpty())
            throw new ApiException("❌ يجب رفع صورة الاستمارة");

        try {
            BufferedImage image = ImageIO.read(formImage.getInputStream());

            BufferedImage processed = enhanceImage(image);

            String text = tesseract.doOCR(processed);

            String ownerName = extractUserNameFromText(text);

            if (ownerName == null || ownerName.isBlank())
                throw new ApiException("❌ لم يتم العثور على اسم المالك");

            String finalName = ownerName;

            // 🔥 لو إنجليزي نحوله عربي
            if (isEnglish(ownerName)) {
                finalName = convertNameToArabic(ownerName);
            }

            if (ownerName == null || ownerName.isBlank())
                throw new ApiException("❌ لم يتم العثور على اسم المالك");

            String normalized = normalizeNameSmart(ownerName);

            Map<String, String> response = new LinkedHashMap<>();
            response.put("originalName", ownerName);
            response.put("normalizedName", normalized);
            response.put("rawText", text);
            response.put("originalName", ownerName); // اللي طلع من OCR
            response.put("normalizedName", finalName); // بعد التحويل
            response.put("rawText", text);

            return response;

        } catch (Exception e) {
            throw new ApiException("❌ فشل استخراج الاسم: " + e.getMessage());
        }
    }

    // =========================================================
    // MANUAL REGISTER
    // =========================================================
    public Map<String, String> registerCarManual(HttpServletRequest request, InpCarDto inpCarDto, MultipartFile formImage) {

        User user = authService.getAuthenticatedUser(request);

        Car car = buildCar(inpCarDto, formImage, user);

        if (carRepository.existsByPlateNumberArabic(car.getPlateNumberArabic()))
            throw new ApiException("❌ هذه اللوحة مسجلة مسبقًا");

        carRepository.save(car);

        return buildResponse(car, user.getFullName());
    }

    // =========================================================
    // GET MY CARS
    // =========================================================
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
            dto.setCarImage(
                    car.getModel().getImagePath() != null
                            ? car.getModel().getImagePath()
                            : "/carimage/default_car.png"
            );
            dtos.add(dto);
        }

        return dtos;
    }

    // =========================================================
    // IMAGE ENHANCEMENT
    // =========================================================
    private BufferedImage enhanceImage(BufferedImage image) {

        BufferedImage gray = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY
        );

        gray.getGraphics().drawImage(image, 0, 0, null);

        return gray;
    }

    // =========================================================
    // NORMALIZE PLATE
    // =========================================================
    private String normalizePlate(String plate) {
        if (plate == null) return null;
        return plate.replaceAll("\\s+", "").trim();
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
                .replace("أ","ا").replace("إ","ا").replace("آ","ا")
                .replace("ة","ه").replace("ى","ي")
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
    // HELPERS
    // =========================================================
    private Integer parseYear(String year) {
        try { return year != null ? Integer.parseInt(year) : null; }
        catch (Exception e) { return null; }
    }

    private Map<String, String> buildResponse(Car car, String ownerName) {

        Map<String, String> map = new LinkedHashMap<>();

        map.put("status", "✅ success");
        map.put("carId", car.getId().toString());
        map.put("ownerName", ownerName);
        map.put("plateArabic", car.getPlateNumberArabic());
        map.put("plateEnglish", car.getPlateNumberEnglish());

        return map;
    }

    private Car buildCar(InpCarDto inpCarDto, MultipartFile formImage, User user) {

        Car car = new Car();

        car.setCarYear(inpCarDto.getCarYear());
        car.setPlateNumberArabic(normalizePlate(inpCarDto.getPlateNumberArabic()));
        car.setPlateNumberEnglish(convertPlateToEnglish(inpCarDto.getPlateNumberArabic()));
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

    private String saveFile(MultipartFile file, String folder) {

        try {
            Path uploadPath = Paths.get("uploads/" + folder);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + folder + "/" + fileName;

        } catch (Exception e) {
            throw new ApiException("❌ فشل حفظ الملف");
        }
    }

    private CarBrand detectBrandFromText(String text) {

        String normalized = normalizeText(text);

        for (CarBrand brand : carBrandRepository.findAll()) {
            if (normalized.contains(normalizeText(brand.getNameAr())) ||
                    normalized.contains(normalizeText(brand.getName()))) {
                return brand;
            }
        }

        throw new ApiException("❌ لم يتم التعرف على الماركة");
    }

    private CarModel detectModelFromText(String text, CarBrand brand) {

        String normalized = normalizeText(text);

        return carModelRepository.findByBrandId(brand.getId()).stream()
                .filter(m ->
                        normalized.contains(normalizeText(m.getNameAr())) ||
                                normalized.contains(normalizeText(m.getName()))
                )
                .findFirst()
                .orElseThrow(() -> new ApiException("❌ لم يتم التعرف على الموديل"));
    }

    public String convertPlateToEnglish(String arabicPlate) {

        arabicPlate = arabicPlate.replace("أ","A").replace("ب","B").replace("ح","J")
                .replace("د","D").replace("ر","R").replace("س","S")
                .replace("ص","X").replace("ط","T").replace("ع","E")
                .replace("ق","G").replace("ك","K").replace("ل","L")
                .replace("م","Z").replace("ن","N").replace("ه","H")
                .replace("و","U").replace("ي","V")
                .replaceAll("\\s+","").trim();

        return arabicPlate;
    }

    // =========================================================
// ENGLISH → ARABIC (SIMPLE TRANSLITERATION)
// =========================================================
    private String convertNameToArabic(String name) {

        if (name == null) return null;

        name = name.toLowerCase();

        return name
                .replace("a","ا")
                .replace("b","ب")
                .replace("c","ك")
                .replace("d","د")
                .replace("e","ي")
                .replace("f","ف")
                .replace("g","ج")
                .replace("h","ه")
                .replace("i","ي")
                .replace("j","ج")
                .replace("k","ك")
                .replace("l","ل")
                .replace("m","م")
                .replace("n","ن")
                .replace("o","و")
                .replace("p","ب")
                .replace("q","ق")
                .replace("r","ر")
                .replace("s","س")
                .replace("t","ت")
                .replace("u","و")
                .replace("v","ف")
                .replace("w","و")
                .replace("x","كس")
                .replace("y","ي")
                .replace("z","ز");
    }
    private boolean isEnglish(String text) {
        return text != null && text.matches(".*[a-zA-Z].*");
    }
    // =========================================================
// NAME NORMALIZATION (PRO LEVEL 🔥)
// =========================================================
    private String normalizeNameSmart(String name) {

        if (name == null) return null;

        name = name.trim().toLowerCase();

        // 🔥 قاموس أسماء شائعة
        Map<String, String> dict = Map.ofEntries(
                Map.entry("ahmed", "احمد"),
                Map.entry("mohammed", "محمد"),
                Map.entry("ali", "علي"),
                Map.entry("abdullah", "عبدالله"),
                Map.entry("abdulrahman", "عبدالرحمن"),
                Map.entry("khalid", "خالد"),
                Map.entry("saad", "سعد"),
                Map.entry("fahad", "فهد"),
                Map.entry("nasser", "ناصر"),
                Map.entry("hassan", "حسن"),
                Map.entry("hussain", "حسين")
        );

        StringBuilder result = new StringBuilder();

        for (String part : name.split(" ")) {

            if (dict.containsKey(part)) {
                result.append(dict.get(part)).append(" ");
            } else {
                result.append(convertNameToArabic(part)).append(" ");
            }
        }

        return result.toString().trim();
    }
}