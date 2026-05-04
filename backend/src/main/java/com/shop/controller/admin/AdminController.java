package com.shop.controller.admin;

import com.shop.entity.Product;
import com.shop.entity.Role;
import com.shop.repository.OrderRepository;
import com.shop.repository.UserRepository;
import com.shop.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Comparator;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final int LOW_STOCK_THRESHOLD = 10;

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductService productService;

    public AdminController(UserRepository userRepository,
                           OrderRepository orderRepository,
                           ProductService productService) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.productService = productService;
    }

    @GetMapping
    public String dashboard(Model model) {
        long userCount = userRepository.count();
        long adminCount = userRepository.countByRole(Role.ADMIN);
        long orderCount = orderRepository.count();
        long revenue = orderRepository.findAll().stream()
                .mapToLong(o -> o.getTotal())
                .sum();

        List<Product> products = productService.findAll();
        long productCount = products.size();
        List<Product> lowStock = products.stream()
                .filter(p -> p.stock() < LOW_STOCK_THRESHOLD)
                .sorted(Comparator.comparingInt(Product::stock))
                .limit(5)
                .toList();

        model.addAttribute("userCount", userCount);
        model.addAttribute("adminCount", adminCount);
        model.addAttribute("orderCount", orderCount);
        model.addAttribute("revenue", revenue);
        model.addAttribute("productCount", productCount);
        model.addAttribute("lowStock", lowStock);
        model.addAttribute("lowStockCount", lowStock.size());
        model.addAttribute("lowStockThreshold", LOW_STOCK_THRESHOLD);
        return "admin/dashboard";
    }
}
