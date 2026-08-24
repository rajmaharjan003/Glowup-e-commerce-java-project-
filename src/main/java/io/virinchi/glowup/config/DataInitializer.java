package io.virinchi.glowup.config;

import io.virinchi.glowup.entity.Category;
import io.virinchi.glowup.entity.Product;
import io.virinchi.glowup.entity.User;
import io.virinchi.glowup.repository.CategoryRepository;
import io.virinchi.glowup.repository.ProductRepository;
import io.virinchi.glowup.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedCatalog();
    }

    private void seedUsers() {
        // Admin User: rajmaharjan738@gmail.com
        if (!userRepository.existsByEmail("rajmaharjan738@gmail.com")) {
            User admin = new User();
            admin.setFullName("Raj Maharjan (Admin)");
            admin.setEmail("rajmaharjan738@gmail.com");
            admin.setPhoneNumber("9801234567");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            admin.setVerified(true);
            admin.setAuthProvider("LOCAL");
            userRepository.save(admin);
            log.info("Admin user created: rajmaharjan738@gmail.com");
        }

        // Demo Admin User: admin@glowup.com
        if (!userRepository.existsByEmail("admin@glowup.com")) {
            User admin = new User();
            admin.setFullName("GlowUp Administrator");
            admin.setEmail("admin@glowup.com");
            admin.setPhoneNumber("9800000000");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            admin.setVerified(true);
            admin.setAuthProvider("LOCAL");
            userRepository.save(admin);
            log.info("Admin user created: admin@glowup.com");
        }
    }

    private void seedCatalog() {
        if (productRepository.count() > 0) {
            log.info("Catalog already seeded with {} products.", productRepository.count());
            return;
        }

        log.info("Seeding initial product catalog and categories into PostgreSQL database...");

        // Categories
        Category catElectronics = getOrCreateCategory("Electronics");
        Category catSmartphones = getOrCreateCategory("Smartphones");
        Category catCosmetics = getOrCreateCategory("Cosmetics");
        Category catFashion = getOrCreateCategory("Fashion");
        Category catFurniture = getOrCreateCategory("Furniture");
        Category catWatches = getOrCreateCategory("Watches");
        Category catSports = getOrCreateCategory("Sports");
        Category catGroceries = getOrCreateCategory("Groceries");
        Category catToys = getOrCreateCategory("Toys");

        // Electronics & Phones
        createProduct("Galaxy S22 Ultra", "Flagship Samsung smartphone with Dynamic AMOLED 2X, S-Pen included and 108MP Quad Camera", 40000.0, 0.0, 25, "https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?w=800&q=80", "Samsung", 4.9, true, true, catSmartphones);
        createProduct("Galaxy S22", "Compact powerhouse with 50MP Pro-grade camera and fast charging", 32999.0, 0.0, 30, "https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=800&q=80", "Samsung", 4.8, true, false, catSmartphones);
        createProduct("Galaxy M13 4GB|64GB", "Budget friendly battery monster with 6000mAh and 50MP triple camera", 10499.0, 5.0, 40, "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&q=80", "Samsung", 4.6, false, false, catSmartphones);
        createProduct("Galaxy M33 4GB|64GB", "Exynos 1280 5G processor with 120Hz smooth display", 16999.0, 0.0, 20, "https://images.unsplash.com/photo-1580910051074-3eb694886505?w=800&q=80", "Samsung", 4.7, false, false, catSmartphones);
        createProduct("Galaxy M53 5G", "Slim 108MP 5G smartphone with Super AMOLED Plus screen", 31999.0, 10.0, 15, "https://images.unsplash.com/photo-1565849904461-04a58ad377e0?w=800&q=80", "Samsung", 4.8, false, false, catSmartphones);
        createProduct("Sony WH-1000XM5 Headphones", "Industry-leading noise cancelling wireless headphones with 30-hour battery life", 35000.0, 0.0, 18, "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&q=80", "Sony", 5.0, true, true, catElectronics);
        createProduct("LG Convection Microwave 28L", "All-in-one convection microwave with auto-cook menu and charcoal lighting heater", 9500.0, 0.0, 10, "https://images.unsplash.com/photo-1585659722983-3a675dabf23d?w=800&q=80", "LG", 4.7, false, false, catElectronics);
        createProduct("Dyson V11 Cordless Vacuum", "Intelligent whole-home cleaning with high torque cleaner head and LCD screen", 8000.0, 0.0, 12, "https://images.unsplash.com/photo-1558317374-067fb5f30001?w=800&q=80", "Dyson", 4.9, true, false, catElectronics);
        createProduct("Philips Digital Rice Cooker", "Fuzzy logic 3D heating rice cooker with non-stick 5-layer pot", 3000.0, 0.0, 22, "https://images.unsplash.com/photo-1544233726-9f1d2b27be8b?w=800&q=80", "Philips", 4.6, false, false, catElectronics);
        createProduct("CG Digital Solo Microwave", "Compact countertop microwave with multi-stage cooking and defrost", 20000.0, 0.0, 8, "https://images.unsplash.com/photo-1574269909862-7e1d70bb8078?w=800&q=80", "CG", 4.5, false, false, catElectronics);

        // Cosmetics
        createProduct("Luxury Beauty Set", "Complete 12-piece skincare and makeup essentials for glowing radiant skin", 1200.0, 0.0, 50, "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=800&q=80", "GlowUp", 4.9, true, true, catCosmetics);
        createProduct("Professional Brush Set", "10 ultra-soft synthetic vegan brushes with leather travel case", 950.0, 0.0, 35, "https://images.unsplash.com/photo-1596462502278-27bfdc403348?w=800&q=80", "Luxe", 4.8, true, false, catCosmetics);
        createProduct("Glow Vitamin C Serum 30ml", "Brightening serum with 10% Vitamin C + Ferulic Acid for hyperpigmentation", 1650.0, 0.0, 45, "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=800&q=80", "GlowUp", 5.0, true, true, catCosmetics);
        createProduct("Velvet Matte Lipstick Duo", "Long-wearing creamy matte lipsticks in Nude Rose & Deep Berry", 690.0, 0.0, 60, "https://images.unsplash.com/photo-1586495777744-4413f21062fa?w=800&q=80", "Velvet", 4.7, false, false, catCosmetics);
        createProduct("Acrylic Vanity Organizer", "360-degree rotating cosmetic and perfume organizer with adjustable tiers", 2400.0, 0.0, 20, "https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=800&q=80", "HomeGlow", 4.8, false, false, catCosmetics);
        createProduct("Eau de Parfum 100ml", "Long lasting luxury fragrance with notes of Bergamot, Jasmine and Amber", 2800.0, 0.0, 15, "https://images.unsplash.com/photo-1592945403244-b3fbafd7f539?w=800&q=80", "Aura", 4.9, true, false, catCosmetics);
        createProduct("All-in-One Makeup Kit", "35 shade eyeshadow palette, blush, contour, and highlight compact", 1890.0, 0.0, 30, "https://images.unsplash.com/photo-1516975080664-ed2fc6a32937?w=800&q=80", "Glamour", 4.8, false, true, catCosmetics);
        createProduct("Hydrating Skincare Set", "4-step Korean beauty routine: Cleanser, Toner, Essence, and Gel Cream", 2400.0, 0.0, 25, "https://images.unsplash.com/photo-1556228720-195a672e8a03?w=800&q=80", "K-Glow", 5.0, true, false, catCosmetics);
        createProduct("Pure Rose Hydrating Toner", "Organic steamed rose water toner for soothing and balancing pH", 580.0, 0.0, 40, "https://images.unsplash.com/photo-1608248597359-2184d284a754?w=800&q=80", "HerbalNepal", 4.7, false, false, catCosmetics);

        // Fashion
        createProduct("Designer Clutch Purse", "Handcrafted premium vegan leather clutch with gold chain strap", 1650.0, 0.0, 20, "https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=800&q=80", "Vogue", 4.8, true, false, catFashion);
        createProduct("Slim Fit Oxford Shirt", "100% combed Egyptian cotton tailored formal shirt", 3299.0, 0.0, 25, "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=800&q=80", "Allen Solly", 4.7, false, false, catFashion);
        createProduct("Heavyweight Vintage Denim Jacket", "Rugged authentic trucker denim jacket with brass buttons", 3899.0, 0.0, 15, "https://images.unsplash.com/photo-1576995853123-5a10305d93c0?w=800&q=80", "Levi's", 4.8, true, false, catFashion);

        // Furniture
        createProduct("Ergonomic High-Back Executive Office Chair", "Breathable mesh ergonomic chair with lumbar support and 3D armrests", 14500.0, 15.0, 12, "https://images.unsplash.com/photo-1580481077190-7361346d180b?w=800&q=80", "FurniCraft", 4.9, true, false, catFurniture);
        createProduct("Solid Sheesham Wood Coffee Table", "Hand-carved premium rosewood center table with natural matte polish", 8999.0, 10.0, 8, "https://images.unsplash.com/photo-1533090161767-e6ffed986b88?w=800&q=80", "Heritage Wood", 4.8, true, false, catFurniture);
        createProduct("Minimalist 3-Seater Nordic Sofa", "High-density foam upholstered sofa with solid oak legs", 32000.0, 20.0, 5, "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=800&q=80", "Nordic Living", 4.9, true, true, catFurniture);
        createProduct("Modern Geometric 5-Tier Bookshelf", "Industrial metal frame and rustic wood display bookshelf", 6500.0, 0.0, 14, "https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=800&q=80", "Urban Shelf", 4.7, false, false, catFurniture);

        // Watches
        createProduct("Apple Watch Ultra 2 GPS + Cellular", "Rugged 49mm titanium case smartwatch with 3000 nits display and dive computer", 52000.0, 20.0, 6, "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800&q=80", "Apple", 5.0, true, true, catWatches);
        createProduct("Titan Edge Ultra-Slim Ceramic Watch", "World's slimmest quartz movement in scratch-resistant sapphire crystal", 22500.0, 0.0, 10, "https://images.unsplash.com/photo-1524805444758-089113d48a6d?w=800&q=80", "Titan", 4.8, true, false, catWatches);
        createProduct("Casio G-Shock Solar Military Stealth", "Shock resistant 200m water resistant tough solar sports watch", 14999.0, 10.0, 18, "https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?w=800&q=80", "Casio", 4.9, false, false, catWatches);
        createProduct("Fossil Gen 6 Smartwatch AMOLED", "Wear OS Snapdragon 4100+ smartwatch with SpO2 and heart rate monitor", 18500.0, 0.0, 12, "https://images.unsplash.com/photo-1508685096489-7aacd43bd3b1?w=800&q=80", "Fossil", 4.7, false, false, catWatches);

        // Sports
        createProduct("Pro Cast Iron Hex Dumbbell Set 20kg", "Anti-roll rubber coated hexagonal dumbbells pair with ergonomic grip", 4800.0, 0.0, 20, "https://images.unsplash.com/photo-1584735935682-2f2b69dff9d2?w=800&q=80", "FlexPro", 4.9, true, false, catSports);
        createProduct("Nivia Storm Match Football & Pump Kit", "FIFA certified 32-panel durable training and match soccer ball", 1450.0, 0.0, 30, "https://images.unsplash.com/photo-1511886929837-354d827aae26?w=800&q=80", "Nivia", 4.8, true, false, catSports);
        createProduct("Yonex Carbonex Badminton Racket Set", "High modulus graphite twin racket pack with full cover and shuttlecocks", 3600.0, 0.0, 25, "https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?w=800&q=80", "Yonex", 4.9, true, true, catSports);
        createProduct("Anti-Tear High Density Yoga & Workout Mat", "Eco-friendly 8mm non-slip textured exercise mat with carry strap", 1200.0, 0.0, 40, "https://images.unsplash.com/photo-1592432678016-e910b452f9a2?w=800&q=80", "ZenFit", 4.7, false, false, catSports);

        // Groceries
        createProduct("Organic Himalayan Orthodox Green Tea 250g", "Single origin premium hand-plucked loose leaf tea from Ilam hills", 850.0, 0.0, 50, "https://images.unsplash.com/photo-1576092768241-dec231879fc3?w=800&q=80", "Himalayan Herbs", 4.9, true, false, catGroceries);
        createProduct("Pure Cold-Pressed Mustard Oil 5L", "Traditional Kolhu pressed unrefined mustard oil rich in natural aroma", 1750.0, 0.0, 30, "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=800&q=80", "Shree Oil", 4.8, true, false, catGroceries);
        createProduct("Premium Royal Basmati Rice 20kg", "Aged long-grain aromatic basmati rice for festive and daily dining", 3200.0, 0.0, 25, "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=800&q=80", "Royal Feast", 4.9, false, false, catGroceries);
        createProduct("Raw Himalayan Wild Honey 500g", "100% pure unfiltered forest honey packed with natural enzymes", 1100.0, 0.0, 35, "https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=800&q=80", "HoneyNepal", 5.0, true, true, catGroceries);

        // Toys
        createProduct("LEGO Technic Supercar Building Kit", "1,200 piece advanced mechanical engineering model with working V8 engine", 9500.0, 10.0, 10, "https://images.unsplash.com/photo-1585366119957-e9730b6d0f60?w=800&q=80", "LEGO", 5.0, true, true, catToys);
        createProduct("RC 4WD High-Speed Rock Crawler Truck", "1:14 scale 2.4GHz remote control waterproof monster truck with rechargeable battery", 4200.0, 0.0, 15, "https://images.unsplash.com/photo-1594787318286-3d835c1d207f?w=800&q=80", "SpeedZone", 4.8, true, false, catToys);
        createProduct("Montessori Wooden Educational Activity Box", "Multi-sensory shape sorting, gears, and counting puzzle for toddlers", 1850.0, 0.0, 25, "https://images.unsplash.com/photo-1566576912321-d58ddd7a6088?w=800&q=80", "WoodyKids", 4.9, false, false, catToys);
        createProduct("Giant 100cm Ultra-Soft Plush Teddy Bear", "Hypoallergenic huggable plush teddy bear with embroidered paws", 2600.0, 0.0, 20, "https://images.unsplash.com/photo-1559454403-b8fb88521f11?w=800&q=80", "FluffyToys", 4.9, true, false, catToys);

        log.info("Successfully seeded {} products into Neon PostgreSQL database!", productRepository.count());
    }

    private Category getOrCreateCategory(String name) {
        return categoryRepository.findByName(name).orElseGet(() -> {
            Category category = new Category();
            category.setName(name);
            return categoryRepository.save(category);
        });
    }

    private void createProduct(String name, String description, double price, double discount, int stock, String image, String brand, double rating, boolean featured, boolean flashSale, Category category) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(description);
        p.setPrice(price);
        p.setDiscount(discount);
        p.setStock(stock);
        p.setImage(image);
        p.setBrand(brand);
        p.setRating(rating);
        p.setFeatured(featured);
        p.setFlashSale(flashSale);
        p.setCategory(category);
        productRepository.save(p);
    }
}