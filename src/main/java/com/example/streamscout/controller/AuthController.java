package com.example.streamscout.controller;

import com.example.streamscout.repository.UserRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public record SignupForm(
      @NotBlank @Size(min = 3, max = 32) String username,
      @NotBlank @Size(min = 8, max = 72) String password
  ) {}

  @GetMapping("/auth/login")
  public String login() {
    return "auth/login";
  }

  @GetMapping("/auth/signup")
  public String signup(Model model) {
    model.addAttribute("form", new SignupForm("", ""));
    return "auth/signup";
  }

  @PostMapping("/auth/signup")
  public String doSignup(
      @ModelAttribute("form") SignupForm form,
      BindingResult binding,
      Model model
  ) {
    if (binding.hasErrors()) return "auth/signup";
    if (userRepository.usernameExists(form.username())) {
      model.addAttribute("error", "Username already exists.");
      return "auth/signup";
    }
    String hash = passwordEncoder.encode(form.password());
    userRepository.createUser(form.username(), hash, "USER");
    return "redirect:/auth/login?signup";
  }
}

