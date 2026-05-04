package com.shop.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Bắt exception toàn cục và render ra trang error đẹp + log có ngữ cảnh.
 *
 * Phân loại:
 *  - {@link OutOfStockException}      → redirect /cart kèm flash message
 *  - {@link ProductNotFoundException} → 404
 *  - {@link NoHandlerFoundException}  → 404 (cần spring.mvc.throw-exception-if-no-handler-found=true)
 *  - {@link IllegalArgumentException} & {@link IllegalStateException} → 400 nếu là API, 500 + log nếu là page
 *  - {@link AccessDeniedException}    → để Spring Security xử lý (không bắt ở đây)
 *  - mọi {@link Exception} khác       → 500
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Hết hàng → quay về giỏ với thông điệp cụ thể (tên SP + số còn lại).
     * User vẫn giữ giỏ hàng, có thể tự sửa quantity.
     */
    @ExceptionHandler(OutOfStockException.class)
    public String handleOutOfStock(OutOfStockException ex, RedirectAttributes redirect) {
        log.warn("Out-of-stock: product={}, requested={}, available={}",
                ex.getProductName(), ex.getRequested(), ex.getAvailable());
        redirect.addFlashAttribute("checkoutError", ex.getMessage());
        return "redirect:/cart";
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public String handleProductNotFound(ProductNotFoundException ex, Model model) {
        log.info("Product not found: {}", ex.getMessage());
        model.addAttribute("status", 404);
        model.addAttribute("message", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public String handleNotFound(NoHandlerFoundException ex, Model model) {
        model.addAttribute("status", 404);
        model.addAttribute("message", "Trang không tồn tại: " + ex.getRequestURL());
        return "error/404";
    }

    /**
     * Logic-level errors (giỏ trống, role không hợp lệ, ...). Không phải bug nên chỉ INFO.
     * Page request → render trang lỗi friendly.
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public String handleBadRequest(RuntimeException ex, Model model, HttpServletRequest req) {
        log.info("Bad request {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        model.addAttribute("status", 400);
        model.addAttribute("message", ex.getMessage());
        return "error/500"; // dùng template chung, status code thay đổi qua model
    }

    /**
     * Để Spring Security xử lý 403 — không nuốt ở đây.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Void> handleAccessDenied(AccessDeniedException ex) {
        throw ex;
    }

    /**
     * Catch-all cho lỗi không ngờ. Log full stack để debug, render 500 page.
     */
    @ExceptionHandler(Exception.class)
    public String handleAny(Exception ex, Model model, HttpServletRequest req) {
        log.error("Unhandled exception at {} {}", req.getMethod(), req.getRequestURI(), ex);
        model.addAttribute("status", 500);
        model.addAttribute("message", "Đã có lỗi xảy ra. Vui lòng thử lại sau.");
        return "error/500";
    }
}
