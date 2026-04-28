package com.example.hotelbooking.controller;

import com.example.hotelbooking.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/signup")
    public String signupForm() {
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signup(@RequestParam String fullName,
                         @RequestParam String email,
                         @RequestParam String password,
                         RedirectAttributes redirectAttributes) {
        if (fullName == null || fullName.isBlank() || email == null || email.isBlank()
                || password == null || password.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Please provide name, email, and a password of at least 6 characters.");
            return "redirect:/signup";
        }
        try {
            authService.registerCustomer(fullName, email, password);
            redirectAttributes.addFlashAttribute("message", "Account created. Please sign in.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/signup";
        }
    }

    @GetMapping("/login")
    public String login(Model model) {
        return "auth/login";
    }
}
