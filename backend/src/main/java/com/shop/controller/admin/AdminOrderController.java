package com.shop.controller.admin;

import com.shop.entity.Order;
import com.shop.entity.OrderStatus;
import com.shop.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private static final List<OrderStatus> STATUS_OPTIONS = List.of(OrderStatus.values());

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("orders", orderService.findAll());
        return "admin/orders/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Order order = orderService.findById(id);
        if (order == null) {
            throw new ResponseStatusException(NOT_FOUND, "Không tìm thấy đơn hàng");
        }
        model.addAttribute("order", order);
        model.addAttribute("statusOptions", STATUS_OPTIONS);
        return "admin/orders/detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam String status,
                               RedirectAttributes redirect) {
        try {
            orderService.updateStatus(id, status);
            redirect.addFlashAttribute("flashSuccess", "Đã cập nhật trạng thái đơn hàng.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }
}
