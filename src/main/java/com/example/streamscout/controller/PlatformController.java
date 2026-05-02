package com.example.streamscout.controller;

import com.example.streamscout.repository.PlatformRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PlatformController {
  private final PlatformRepository platformRepository;

  public PlatformController(PlatformRepository platformRepository) {
    this.platformRepository = platformRepository;
  }

  @GetMapping("/platforms")
  public String platforms(Model model) {
    model.addAttribute("stats", platformRepository.getPlatformStats());
    return "platforms/platforms";
  }
}

