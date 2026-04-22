package org.example.tears.Config;

import lombok.RequiredArgsConstructor;
import org.example.tears.Model.CarBrand;
import org.example.tears.Model.CarModel;
import org.example.tears.Repository.CarBrandRepository;
import org.example.tears.Repository.CarModelRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CarBrandRepository brandRepo;
    private final CarModelRepository modelRepo;

    @Override
    public void run(String... args) {

        if (brandRepo.count() > 0) {
            return;
        }

// ================== Brands ==================
        CarBrand toyota = createBrand("Toyota", "تويوتا", "/brands/toyota.png");
        CarBrand hyundai = createBrand("Hyundai", "هونداي", "/brands/hyundai.png");
        CarBrand honda = createBrand("Honda", "هواندا", "/brands/honda.png");
        CarBrand ford = createBrand("Ford", "فورد", "/brands/ford.png");
        CarBrand cadillac = createBrand("Cadillac", "كاديلاك", "/brands/cadillac.png");
        CarBrand porsche = createBrand("Porsche", "بورش", "/brands/porsche.png");
        CarBrand bmw = createBrand("BMW", "بي إم دبليو", "/brands/bmw.png");
        CarBrand mercedes = createBrand("Mercedes", "مرسيدس", "/brands/mercedes.png");
        CarBrand miniCooper = createBrand("Mini Cooper", "ميني كوبر", "/brands/mini.png");
        CarBrand jaguar = createBrand("Jaguar", "جاكوار", "/brands/jaguar.png");
        CarBrand gmc = createBrand("GMC", "جي إم سي", "/brands/gmc.png");
        CarBrand jeep = createBrand("Jeep", "جيب", "/brands/jeep.png");
        CarBrand tesla = createBrand("Tesla", "تسلا", "/brands/tesla.png");
        CarBrand lexus = createBrand("Lexus", "لكزس", "/brands/lexus.png");
        CarBrand fiat = createBrand("Fiat", "فيات", "/brands/fiat.png");
        CarBrand landRover = createBrand("Land Rover", "لاند روفر", "/brands/land_rover.png");
        CarBrand mg = createBrand("MG", "إم جي", "/brands/mg.png");
        CarBrand haval = createBrand("Haval", "هافال", "/brands/haval.png");
        CarBrand infiniti = createBrand("Infiniti", "إنفينيتي", "/brands/infiniti.png");
        CarBrand rollsRoyce = createBrand("Rolls Royce", "رولز رويس", "/brands/rolls_royce.png");
        CarBrand volvo = createBrand("Volvo", "فولفو", "/brands/volvo.png");
        CarBrand bentley = createBrand("Bentley", "بنتلي", "/brands/bentley.png");
        CarBrand genesis = createBrand("Genesis", "جينيسس", "/brands/genesis.png");
        CarBrand byd = createBrand("BYD", "بي واي دي", "/brands/byd.png");

// ================== Toyota ==================
        createModel("Camry", "كامري", toyota);
        createModel("Corolla", "كورولا", toyota);
        createModel("Yaris", "يارس", toyota);
        createModel("Avalon", "أفالون", toyota);
        createModel("Hilux", "هايلكس", toyota);

// ================== Hyundai ==================
        createModel("Accent", "أكسنت", hyundai);
        createModel("Elantra", "إلنترا", hyundai);
        createModel("Sonata", "سوناتا", hyundai);
        createModel("Tucson", "توكسون", hyundai);
        createModel("Santa Fe", "سانتا في", hyundai);

// ================== Honda ==================
        createModel("Civic", "سيفيك", honda);
        createModel("Accord", "أكورد", honda);
        createModel("CR-V", "سي أر في", honda);
        createModel("HR-V", "إتش أر في", honda);

// ================== Ford ==================
        createModel("Focus", "فوكوس", ford);
        createModel("Explorer", "إكسبلورر", ford);
        createModel("Mustang", "موستنج", ford);
        createModel("F-150", "إف 150", ford);

// ================== Cadillac ==================
        createModel("Escalade", "إسكاليد", cadillac);
        createModel("XT5", "XT5", cadillac);
        createModel("CT5", "CT5", cadillac);
        createModel("CTS", "CTS", cadillac);

// ================== Porsche ==================
        createModel("911", "911", porsche);
        createModel("Cayenne", "كاين", porsche);
        createModel("Macan", "ماكان", porsche);
        createModel("Panamera", "باناميرا", porsche);

// ================== BMW ==================
        createModel("Series 3", "سلسلة 3", bmw);
        createModel("Series 5", "سلسلة 5", bmw);
        createModel("X5", "X5", bmw);
        createModel("Z4", "Z4", bmw);

// ================== Mercedes ==================
        createModel("C-Class", "سي كلاس", mercedes);
        createModel("E-Class", "إي كلاس", mercedes);
        createModel("GLC", "GLC", mercedes);
        createModel("G-Class", "G كلاس", mercedes);

// ================== Mini Cooper ==================
        createModel("Hardtop 2-Door", "هاردتوب 2 باب", miniCooper);
        createModel("Hardtop 4-Door", "هاردتوب 4 باب", miniCooper);
        createModel("Convertible", "قابل للتحويل", miniCooper);
        createModel("Countryman", "كانتري مان", miniCooper);

// ================== Jaguar ==================
        createModel("XE", "XE", jaguar);
        createModel("XF", "XF", jaguar);
        createModel("F-Pace", "إف بيس", jaguar);
        createModel("F-Type", "إف تايب", jaguar);

// ================== GMC ==================
        createModel("Sierra", "سييرا", gmc);
        createModel("Yukon", "يوكون", gmc);
        createModel("Terrain", "تيرين", gmc);
        createModel("Acadia", "أكاديا", gmc);

// ================== Jeep ==================
        createModel("Wrangler", "رنجلر", jeep);
        createModel("Grand Cherokee", "جراند شيروكي", jeep);
        createModel("Compass", "كومباس", jeep);
        createModel("Cherokee", "شيروكي", jeep);

// ================== Tesla ==================
        createModel("Model S", "موديل S", tesla);
        createModel("Model 3", "موديل 3", tesla);
        createModel("Model X", "موديل X", tesla);
        createModel("Model Y", "موديل Y", tesla);

// ================== Lexus ==================
        createModel("IS", "IS", lexus);
        createModel("ES", "ES", lexus);
        createModel("RX", "RX", lexus);
        createModel("LX", "LX", lexus);

// ================== Fiat ==================
        createModel("500", "500", fiat);
        createModel("Panda", "باندا", fiat);
        createModel("Tipo", "تيبو", fiat);
        createModel("Punto", "بونتو", fiat);

// ================== Land Rover ==================
        createModel("Defender", "ديفيندر", landRover);
        createModel("Discovery", "ديسكفري", landRover);
        createModel("Range Rover", "رينج روفر", landRover);
        createModel("Evoque", "إيفوك", landRover);

// ================== MG ==================
        createModel("ZS", "ZS", mg);
        createModel("HS", "HS", mg);
        createModel("Hector", "هيكتور", mg);
        createModel("Marvel R", "مارفل R", mg);

// ================== Haval ==================
        createModel("H6", "H6", haval);
        createModel("Jolion", "جوليون", haval);
        createModel("H9", "H9", haval);
        createModel("F7", "F7", haval);

// ================== Infiniti ==================
        createModel("Q50", "Q50", infiniti);
        createModel("Q60", "Q60", infiniti);
        createModel("QX50", "QX50", infiniti);
        createModel("QX80", "QX80", infiniti);

// ================== Rolls Royce ==================
        createModel("Phantom", "فانتوم", rollsRoyce);
        createModel("Ghost", "جوست", rollsRoyce);
        createModel("Wraith", "رايث", rollsRoyce);
        createModel("Cullinan", "كولنان", rollsRoyce);

// ================== Volvo ==================
        createModel("XC40", "XC40", volvo);
        createModel("XC60", "XC60", volvo);
        createModel("XC90", "XC90", volvo);
        createModel("S90", "S90", volvo);

// ================== Bentley ==================
        createModel("Continental GT", "كونتيننتال GT", bentley);
        createModel("Flying Spur", "فلاينج سبور", bentley);
        createModel("Bentayga", "بينتايغا", bentley);
        createModel("Mulsanne", "مولسان", bentley);

// ================== Genesis ==================
        createModel("G70", "G70", genesis);
        createModel("G80", "G80", genesis);
        createModel("G90", "G90", genesis);
        createModel("GV80", "GV80", genesis);

// ================== BYD ==================
        createModel("Tang", "تانغ", byd);
        createModel("Yuan", "يوان", byd);
        createModel("Song", "سونغ", byd);
        createModel("Han", "هان", byd);
    }

        // =========================
    // Helpers
    // =========================
    private CarBrand createBrand(String name, String nameAr, String logoPath) {

        return brandRepo.findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    CarBrand brand = new CarBrand();

                    brand.setName(name);                 // EN
                    brand.setNameAr(nameAr);               // 👈 مؤقتًا نفس الاسم
                    brand.setSlug(generateSlug(name));   // 👈 مهم
                    brand.setLogoPath(logoPath);

                    return brandRepo.save(brand);
                });
    }
    private String generateSlug(String name) {
        return name
                .toLowerCase()
                .trim()
                .replaceAll("\\s+", "-"); // spaces → -
    }

    private void createModel(String nameEn, String nameAr, CarBrand brand) {
        modelRepo.findByNameIgnoreCaseAndBrandId(nameAr, brand.getId())
                .orElseGet(() -> {
                    CarModel m = new CarModel();
                    m.setName(nameEn);
                    m.setNameAr(nameAr);
                    m.setImagePath("/carimage/" + brand.getName().toLowerCase() + "_" + nameEn.toLowerCase() + ".png");
                    String slug = nameEn.toLowerCase().replace(" ", "-");
                    m.setSlug(slug);
                    m.setBrand(brand);
                    return modelRepo.save(m);
                });
    }



}
