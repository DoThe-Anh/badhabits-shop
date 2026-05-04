package com.shop.controller.admin;

import com.shop.entity.Category;
import com.shop.repository.CategoryRepository;
import com.shop.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public AdminCategoryController(CategoryRepository categoryRepository,
                                   ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/categories/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("isEdit", false);
        return "admin/categories/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("category") Category category,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        // Custom rule ngoài bean validation: slug phải unique
        if (category.slug() != null && !category.slug().isBlank()
                && categoryRepository.existsBySlug(category.slug())) {
            result.addError(new FieldError("category", "slug",
                    category.slug(), false, null, null,
                    "Slug \"" + category.slug() + "\" đã tồn tại"));
        }

        if (result.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "admin/categories/form";
        }

        categoryRepository.save(category);
        redirect.addFlashAttribute("flashSuccess",
                "Đã thêm danh mục \"" + category.name() + "\".");
        return "redirect:/admin/categories";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category == null) {
            redirect.addFlashAttribute("flashError", "Không tìm thấy danh mục #" + id);
            return "redirect:/admin/categories";
        }
        model.addAttribute("category", category);
        model.addAttribute("isEdit", true);
        return "admin/categories/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("category") Category form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        Category target = categoryRepository.findById(id).orElse(null);
        if (target == null) {
            redirect.addFlashAttribute("flashError", "Không tìm thấy danh mục #" + id);
            return "redirect:/admin/categories";
        }

        // Slug unique check — chỉ báo lỗi nếu slug đổi sang slug khác đã có
        if (form.slug() != null && !form.slug().equals(target.slug())
                && categoryRepository.existsBySlug(form.slug())) {
            result.addError(new FieldError("category", "slug",
                    form.slug(), false, null, null,
                    "Slug \"" + form.slug() + "\" đã tồn tại"));
        }

        if (result.hasErrors()) {
            model.addAttribute("isEdit", true);
            // form binding đã set ID = null cho `form`, gán lại cho template hiển thị URL post đúng
            form.setSlug(form.slug());
            model.addAttribute("category", target); // dùng target để có ID, nhưng giữ giá trị form
            target.setSlug(form.slug());
            target.setName(form.name());
            target.setDescription(form.description());
            return "admin/categories/form";
        }

        target.setSlug(form.slug());
        target.setName(form.name());
        target.setDescription(form.description());
        categoryRepository.save(target);
        redirect.addFlashAttribute("flashSuccess",
                "Đã cập nhật \"" + target.name() + "\".");
        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category == null) return "redirect:/admin/categories";

        long inUse = productRepository.countByCategory(category);
        if (inUse > 0) {
            redirect.addFlashAttribute("flashError",
                    "Không xoá được danh mục \"" + category.name() + "\" — còn " + inUse +
                    " sản phẩm đang dùng. Đổi danh mục cho các sản phẩm đó trước rồi xoá.");
            return "redirect:/admin/categories";
        }

        categoryRepository.delete(category);
        redirect.addFlashAttribute("flashSuccess", "Đã xoá danh mục \"" + category.name() + "\".");
        return "redirect:/admin/categories";
    }
}
