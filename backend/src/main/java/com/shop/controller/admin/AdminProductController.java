package com.shop.controller.admin;

import com.shop.entity.Color;
import com.shop.entity.Product;
import com.shop.repository.ProductRepository;
import com.shop.service.CategoryService;
import com.shop.service.FileUploadService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private static final int LOW_STOCK_THRESHOLD = 10;
    private static final int MAX_PAGE_SIZE = 100;

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final FileUploadService fileUploadService;

    public AdminProductController(ProductRepository productRepository,
                                  CategoryService categoryService,
                                  FileUploadService fileUploadService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
        this.fileUploadService = fileUploadService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) String category,
                       @RequestParam(required = false, defaultValue = "name") String sort,
                       @RequestParam(required = false, defaultValue = "0") int page,
                       @RequestParam(required = false, defaultValue = "20") int size,
                       Model model) {

        // Sanity-clamp page/size để chống abuse (?size=99999)
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);

        // Normalize q + category để query null/empty cho như nhau
        String qNorm   = (q == null        || q.isBlank())        ? null : q.trim();
        String catNorm = (category == null || category.isBlank()) ? null : category.trim();

        Sort sortSpec = switch (sort) {
            case "stock-asc"  -> Sort.by(Sort.Direction.ASC,  "stock");
            case "stock-desc" -> Sort.by(Sort.Direction.DESC, "stock");
            case "price-asc"  -> Sort.by(Sort.Direction.ASC,  "price");
            case "price-desc" -> Sort.by(Sort.Direction.DESC, "price");
            default           -> Sort.by(Sort.Direction.ASC,  "name");
        };
        Pageable pageable = PageRequest.of(safePage, safeSize, sortSpec);

        // 1 query duy nhất xử lý cả search + filter + sort + page
        Page<Product> productPage = productRepository.search(qNorm, catNorm, pageable);

        // Aggregate stats — query riêng, không load toàn bộ rows
        long totalCount  = productRepository.count();
        long totalStock  = productRepository.sumAllStock();
        long lowStockCnt = productRepository.countByStockLessThan(LOW_STOCK_THRESHOLD);

        Map<String, String> categoryNames = categoryService.findAll().stream()
                .collect(Collectors.toMap(c -> c.slug(), c -> c.name(), (a, b) -> a));

        model.addAttribute("productPage", productPage);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("categoryNames", categoryNames);
        model.addAttribute("selectedQuery", q == null ? "" : q);
        model.addAttribute("selectedCategory", category == null ? "" : category);
        model.addAttribute("selectedSort", sort);
        model.addAttribute("selectedSize", safeSize);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("filteredCount", productPage.getTotalElements());
        model.addAttribute("totalStock", totalStock);
        model.addAttribute("lowStockCount", lowStockCnt);
        model.addAttribute("lowStockThreshold", LOW_STOCK_THRESHOLD);
        return "admin/products/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("isEdit", false);
        return "admin/products/form";
    }

    @PostMapping
    public String create(HttpServletRequest request,
                         @RequestParam(required = false) MultipartFile primaryImageFile,
                         @RequestParam(required = false) MultipartFile hoverImageFile,
                         Model model,
                         RedirectAttributes redirect) {
        Product product = new Product();
        try {
            applyForm(product, request, primaryImageFile, hoverImageFile);
            validateProduct(product, null);
            productRepository.save(product);
            redirect.addFlashAttribute("flashSuccess",
                    "Đã thêm sản phẩm \"" + product.name() + "\".");
            return "redirect:/admin/products";
        } catch (IllegalArgumentException e) {
            // Re-render form giữ lại giá trị đã nhập + hiển thị error
            model.addAttribute("product", product);
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("isEdit", false);
            model.addAttribute("flashError", e.getMessage());
            return "admin/products/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            redirect.addFlashAttribute("flashError", "Không tìm thấy sản phẩm #" + id);
            return "redirect:/admin/products";
        }
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("isEdit", true);
        return "admin/products/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         HttpServletRequest request,
                         @RequestParam(required = false) MultipartFile primaryImageFile,
                         @RequestParam(required = false) MultipartFile hoverImageFile,
                         Model model,
                         RedirectAttributes redirect) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            redirect.addFlashAttribute("flashError", "Không tìm thấy sản phẩm #" + id);
            return "redirect:/admin/products";
        }
        String originalSlug = product.slug();
        try {
            applyForm(product, request, primaryImageFile, hoverImageFile);
            validateProduct(product, originalSlug);
            productRepository.save(product);
            redirect.addFlashAttribute("flashSuccess",
                    "Đã cập nhật \"" + product.name() + "\".");
            return "redirect:/admin/products";
        } catch (IllegalArgumentException e) {
            model.addAttribute("product", product);
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("isEdit", true);
            model.addAttribute("flashError", e.getMessage());
            return "admin/products/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        productRepository.findById(id).ifPresent(p -> {
            productRepository.delete(p);
            redirect.addFlashAttribute("flashSuccess", "Đã xoá sản phẩm \"" + p.name() + "\".");
        });
        return "redirect:/admin/products";
    }

    /**
     * Validate product sau khi đã apply form.
     * @param p product đã được fill từ form
     * @param currentSlug slug cũ (khi update) — null khi create
     */
    private void validateProduct(Product p, String currentSlug) {
        List<String> errors = new ArrayList<>();

        if (p.name() == null || p.name().isBlank()) {
            errors.add("Tên sản phẩm bắt buộc");
        } else if (p.name().length() > 500) {
            errors.add("Tên tối đa 500 ký tự");
        }

        if (p.slug() == null || p.slug().isBlank()) {
            errors.add("Slug bắt buộc");
        } else if (!p.slug().matches("[a-z0-9-]+")) {
            errors.add("Slug chỉ được chứa chữ thường, số, dấu gạch ngang");
        } else if (p.slug().length() > 200) {
            errors.add("Slug tối đa 200 ký tự");
        } else if (!p.slug().equals(currentSlug) && productRepository.existsBySlug(p.slug())) {
            errors.add("Slug \"" + p.slug() + "\" đã tồn tại");
        }

        if (p.category() == null) {
            errors.add("Phải chọn danh mục hợp lệ");
        }

        if (p.price() < 0) errors.add("Giá phải >= 0");
        if (p.originalPrice() != null && p.originalPrice() < 0) errors.add("Giá gốc phải >= 0");
        if (p.originalPrice() != null && p.originalPrice() <= p.price()) {
            errors.add("Giá gốc phải > giá bán (để hiển thị sale)");
        }

        if (p.primaryImage() == null || p.primaryImage().isBlank()) {
            errors.add("Phải có ảnh chính (upload file hoặc dán URL)");
        }

        if (p.stock() < 0) errors.add("Tồn kho phải >= 0");

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(" · ", errors));
        }
    }

    /** Map form fields → Product (dùng chung cho create + update). */
    private void applyForm(Product product, HttpServletRequest req,
                           MultipartFile primaryImageFile, MultipartFile hoverImageFile) {
        product.setName(req.getParameter("name"));
        product.setSlug(req.getParameter("slug"));
        // Resolve Category bằng slug — không tìm thấy → null, sẽ bị validate bắt sau.
        String catSlug = req.getParameter("categorySlug");
        if (catSlug != null && !catSlug.isBlank()) {
            product.setCategory(categoryService.findBySlug(catSlug).orElse(null));
        } else {
            product.setCategory(null);
        }
        product.setCollection(emptyToNull(req.getParameter("collection")));
        product.setPrice(parseLong(req.getParameter("price"), 0L));

        String origStr = req.getParameter("originalPrice");
        product.setOriginalPrice(origStr == null || origStr.isBlank() ? null : parseLong(origStr, 0L));

        product.setDescription(emptyToNull(req.getParameter("description")));
        product.setMaterial(emptyToNull(req.getParameter("material")));
        product.setStock((int) parseLong(req.getParameter("stock"), 0L));
        product.setNew("on".equalsIgnoreCase(req.getParameter("isNew")) || "true".equalsIgnoreCase(req.getParameter("isNew")));
        product.setFeatured("on".equalsIgnoreCase(req.getParameter("isFeatured")) || "true".equalsIgnoreCase(req.getParameter("isFeatured")));

        // primaryImage: ưu tiên file upload, fallback URL nhập
        String savedPrimary = fileUploadService.save(primaryImageFile);
        if (savedPrimary != null) {
            product.setPrimaryImage(savedPrimary);
        } else {
            String url = req.getParameter("primaryImageUrl");
            if (url != null && !url.isBlank()) product.setPrimaryImage(url.trim());
        }

        String savedHover = fileUploadService.save(hoverImageFile);
        if (savedHover != null) {
            product.setHoverImage(savedHover);
        } else {
            String url = req.getParameter("hoverImageUrl");
            if (url != null && !url.isBlank()) product.setHoverImage(url.trim());
        }

        // sizes: csv → list
        product.setSizes(splitCsv(req.getParameter("sizes")));

        // gallery: 1 url / dòng
        product.setGallery(splitLines(req.getParameter("gallery")));

        // colors: 1 dòng "NAME,HEX"
        List<Color> colors = new ArrayList<>();
        String colorsRaw = req.getParameter("colors");
        if (colorsRaw != null) {
            for (String line : colorsRaw.split("\\r?\\n")) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2 && !parts[0].isBlank() && !parts[1].isBlank()) {
                    colors.add(new Color(parts[0].trim(), parts[1].trim()));
                }
            }
        }
        product.setColors(colors);
    }

    private static long parseLong(String s, long fallback) {
        try { return Long.parseLong(s.trim()); }
        catch (NumberFormatException | NullPointerException e) { return fallback; }
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private static List<String> splitCsv(String s) {
        if (s == null || s.isBlank()) return new ArrayList<>();
        return Arrays.stream(s.split(","))
                .map(String::trim).filter(x -> !x.isBlank())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static List<String> splitLines(String s) {
        if (s == null || s.isBlank()) return new ArrayList<>();
        return Arrays.stream(s.split("\\r?\\n"))
                .map(String::trim).filter(x -> !x.isBlank())
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
