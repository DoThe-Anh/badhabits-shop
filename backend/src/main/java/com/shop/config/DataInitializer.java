package com.shop.config;

import com.shop.entity.Category;
import com.shop.entity.Color;
import com.shop.entity.Product;
import com.shop.entity.Role;
import com.shop.entity.User;
import com.shop.repository.CategoryRepository;
import com.shop.repository.ProductRepository;
import com.shop.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    public DataInitializer(UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           ProductRepository productRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedAdmin();
        seedCategories();
        seedProducts();
    }

    // ---------- Admin bootstrap ----------
    private void seedAdmin() {
        if (userRepository.countByRole(Role.ADMIN) > 0) return;
        if (adminPassword == null || adminPassword.isBlank()) {
            log.warn("⚠️  ADMIN_PASSWORD chưa set trong backend/.env — bỏ qua seed admin. " +
                    "Tạo file backend/.env với ADMIN_PASSWORD=... rồi restart để bootstrap admin đầu tiên.");
            return;
        }
        User admin = new User(
                adminEmail,
                passwordEncoder.encode(adminPassword),
                "Administrator",
                "",
                "",
                Role.ADMIN,
                Instant.now()
        );
        userRepository.save(admin);
        log.warn("Seeded admin từ .env: {} — đổi mật khẩu sau khi login lần đầu.", adminEmail);
    }

    // ---------- Categories ----------
    private void seedCategories() {
        if (categoryRepository.count() > 0) return;
        categoryRepository.saveAll(List.of(
                new Category("tops",        "TOPS",        "Áo thun, áo sơ mi, hoodie"),
                new Category("bottoms",     "BOTTOMS",     "Quần short, quần dài, quần jogger"),
                new Category("outerwear",   "OUTERWEAR",   "Áo khoác, áo gió, jacket"),
                new Category("accessories", "ACCESSORIES", "Phụ kiện, nón, túi")
        ));
        log.warn("Seeded 4 categories.");
    }

    /** Map slug → Category, populate trước khi seed products để build() lookup. */
    private Map<String, Category> categoriesBySlug;

    // ---------- Products ----------
    private void seedProducts() {
        if (productRepository.count() > 0) return;

        categoriesBySlug = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::slug, c -> c));

        // Chỉ giữ những màu/size mà 10 sản phẩm seed đang dùng.
        // Khi thêm sản phẩm mới qua admin form, bạn nhập màu trực tiếp ở UI.
        final Color BLACK = new Color("BLACK", "#0a0a0a");
        final Color WHITE = new Color("WHITE", "#f4f4f2");
        final Color CREAM = new Color("CREAM", "#e8dfc9");

        final List<String> APPAREL = List.of("S", "M", "L", "XL");

        List<Product> products = new ArrayList<>();

        // ----- Mùa hè: áo thun, áo sơ mi cộc, quần short -----
        products.add(build("set-ao-thun-short-astro-ape", "Set áo thun + quần short Astro Ape Journey",
                "tops", "HO25", 450_000L, null,
                "/images/products/set-astro-ape.webp",
                List.of(BLACK), APPAREL,
                "Set áo thun + quần short đồng bộ. Áo in graffiti Astro Ape, quần in logo bên đùi. Phong cách streetwear cao cấp.",
                "100% cotton 220gsm. Quần vải mềm co giãn nhẹ.",
                25, true, true));

        products.add(build("set-ao-thun-short-trend-gradient", "Set áo thun gradient + quần short Start",
                "tops", "HO25", 320_000L, null,
                "/images/products/set-trend-gradient.jpg",
                List.of(WHITE, BLACK), APPAREL,
                "Set áo thun gradient trắng-đen in chữ \"I'M TREND\" + quần short đen phối chữ \"START\". Trendy cho giới trẻ.",
                "95% cotton, 5% spandex. Quần polyester co giãn.",
                30, true, true));

        products.add(build("set-ao-thun-short-ny-basic", "Set áo thun + quần short NY Basic",
                "tops", "HO25", 280_000L, null,
                "/images/products/set-ny-basic.webp",
                List.of(BLACK), APPAREL,
                "Set áo thun oversize + quần short đen phối logo NY trắng. Tối giản, dễ phối, mặc hàng ngày cực chill.",
                "100% cotton chải 200gsm.",
                40, true, false));

        products.add(build("set-ao-thun-short-paris-athletic", "Set áo thun + quần short Paris Athletic",
                "tops", "HO25", 390_000L, null,
                "/images/products/set-paris-athletic.webp",
                List.of(CREAM, WHITE), APPAREL,
                "Set tone kem trầm in chữ \"PARIS Athletic Dept\", phong cách college năng động.",
                "Cotton pha 80/20, thoáng mát, hút ẩm tốt.",
                28, true, true));

        products.add(build("set-ao-thun-short-space-shin", "Set áo thun + quần short Space Outer",
                "tops", "HO25", 299_000L, null,
                "/images/products/set-space-shin.webp",
                List.of(WHITE), APPAREL,
                "Set trắng in hình Shin-chan phi hành gia cute, chữ \"SPACE OUTER MANS\". Dành cho các bạn teen.",
                "100% cotton chải 200gsm.",
                35, true, false));

        products.add(build("set-ao-thun-short-rkton", "Set áo thun + quần short Rkton Graffiti",
                "tops", "HIT BACK", 360_000L, null,
                "/images/products/set-rkton.webp",
                List.of(BLACK), APPAREL,
                "Set đen in graffiti \"RKTON\" và họa tiết brushstroke trắng, vibe underground chuẩn.",
                "Cotton 220gsm. Quần vải thun lạnh co giãn.",
                22, true, true));

        products.add(build("set-ao-thun-short-dsquared-pineapple", "Set áo thun + quần short Dsquared Pineapple",
                "tops", "HO25", 490_000L, null,
                "/images/products/set-dsquared-pineapple.webp",
                List.of(BLACK, WHITE), APPAREL,
                "Set đen/trắng thêu pineapple + bulldog + chữ \"D2\" — phối gợi Dsquared2 premium.",
                "Cotton 220gsm. Chi tiết thêu tay.",
                18, true, true));

        products.add(build("set-ao-polo-short-b-stripe", "Set áo polo + quần short B Stripe",
                "tops", "HO25", 350_000L, null,
                "/images/products/set-b-polo-stripe.webp",
                List.of(BLACK), APPAREL,
                "Set áo polo đen phối sọc trắng chữ B, mặc lịch sự mà vẫn năng động.",
                "Cotton pique 210gsm. Quần thun co giãn.",
                26, true, true));

        products.add(build("set-ao-thun-short-white-vogue", "Set áo thun + quần short White Vogue Texture",
                "tops", "HO25", 329_000L, null,
                "/images/products/set-white-vogue.webp",
                List.of(WHITE), APPAREL,
                "Set trắng vải dệt nổi hoa văn chữ Vogue, form oversize mát mẻ cho mùa hè.",
                "Vải jacquard 3D 250gsm.",
                22, true, false));

        products.add(build("set-ao-thun-short-mtag-astronaut", "Set áo thun + quần short M-TAG Astronaut",
                "tops", "HO25", 310_000L, null,
                "/images/products/set-mtag-astronaut.webp",
                List.of(BLACK), APPAREL,
                "Set đen in phi hành gia trượt ván chữ \"M-TAG / HAPPY\", quần phối logo Playboy.",
                "Cotton 220gsm. Quần thun lạnh.",
                24, true, true));

        productRepository.saveAll(products);
        log.warn("Seeded {} products.", products.size());
    }

    /**
     * Tạo Product với image URLs build từ photoId:
     * - photoId bắt đầu "/" hoặc "http" → dùng nguyên (local hoặc absolute URL).
     * - Còn lại → coi là Unsplash photo ID, build CDN URL với param size/crop.
     * categorySlug được lookup từ {@link #categoriesBySlug} → Category entity.
     */
    private Product build(String slug, String name, String categorySlug, String collection,
                          long price, Long originalPrice,
                          String photoId,
                          List<Color> colors, List<String> sizes,
                          String description, String material,
                          int stock, boolean isNew, boolean isFeatured) {
        final String primary, hover;
        final List<String> gallery;

        if (photoId.startsWith("/") || photoId.startsWith("http")) {
            primary = photoId;
            hover = photoId;
            gallery = List.of(photoId, photoId, photoId, photoId);
        } else {
            String base = "https://images.unsplash.com/" + photoId;
            primary = base + "?w=600&q=80&auto=format&fit=crop";
            hover = base + "?w=600&q=80&auto=format&fit=crop&crop=bottom";
            gallery = List.of(
                    base + "?w=900&q=85&auto=format&fit=crop",
                    base + "?w=900&q=85&auto=format&fit=crop&crop=top",
                    base + "?w=900&q=85&auto=format&fit=crop&crop=bottom",
                    base + "?w=900&q=85&auto=format&fit=crop&crop=entropy"
            );
        }
        Category category = categoriesBySlug.get(categorySlug);
        if (category == null) {
            throw new IllegalStateException("Seed product reference unknown category slug: " + categorySlug);
        }
        return new Product(slug, name, category, collection, price, originalPrice,
                primary, hover, gallery, sizes, colors, description, material,
                stock, isNew, isFeatured);
    }
}
