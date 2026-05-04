package com.shop.controller.admin;

import com.shop.entity.Role;
import com.shop.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin/users/list";
    }

    @PostMapping("/{id}/promote")
    public String promote(@PathVariable Long id,
                          @AuthenticationPrincipal UserDetails principal,
                          RedirectAttributes redirect) {
        return changeRole(id, Role.ADMIN, principal, redirect);
    }

    @PostMapping("/{id}/demote")
    public String demote(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails principal,
                         RedirectAttributes redirect) {
        return changeRole(id, Role.USER, principal, redirect);
    }

    @PostMapping("/{id}/lock")
    public String lock(@PathVariable Long id,
                       @AuthenticationPrincipal UserDetails principal,
                       RedirectAttributes redirect) {
        return setEnabled(id, false, principal, redirect);
    }

    @PostMapping("/{id}/unlock")
    public String unlock(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails principal,
                         RedirectAttributes redirect) {
        return setEnabled(id, true, principal, redirect);
    }

    private String setEnabled(Long id, boolean enabled, UserDetails principal, RedirectAttributes redirect) {
        String actor = principal == null ? "" : principal.getUsername();
        try {
            userService.setEnabled(id, enabled, actor);
            redirect.addFlashAttribute("flashSuccess",
                    enabled ? "Đã mở khoá tài khoản." : "Đã khoá tài khoản.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    private String changeRole(Long id, Role role, UserDetails principal, RedirectAttributes redirect) {
        String actor = principal == null ? "" : principal.getUsername();
        try {
            userService.changeRole(id, role, actor);
            redirect.addFlashAttribute("flashSuccess",
                    role == Role.ADMIN ? "Đã promote user thành admin." : "Đã demote admin về user thường.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
