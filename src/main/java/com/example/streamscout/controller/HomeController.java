package com.example.streamscout.controller;

import com.example.streamscout.repository.MovieRepository;
import com.example.streamscout.repository.PlatformRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
  private final MovieRepository movieRepository;
  private final PlatformRepository platformRepository;

  public HomeController(MovieRepository movieRepository, PlatformRepository platformRepository) {
    this.movieRepository = movieRepository;
    this.platformRepository = platformRepository;
  }

  @GetMapping("/")
  public String home(Model model) {
    model.addAttribute("platforms", platformRepository.listPlatformNames());
    model.addAttribute("trending", movieRepository.search(null, null, null, null, null, null, 12));
    return "index";
  }
}

