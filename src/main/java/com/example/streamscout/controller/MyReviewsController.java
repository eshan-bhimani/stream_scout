package com.example.streamscout.controller;

import com.example.streamscout.repository.ReviewRepository;
import com.example.streamscout.repository.UserLookupRepository;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MyReviewsController {
  private final ReviewRepository reviewRepository;
  private final UserLookupRepository userLookupRepository;

  public MyReviewsController(ReviewRepository reviewRepository, UserLookupRepository userLookupRepository) {
    this.reviewRepository = reviewRepository;
    this.userLookupRepository = userLookupRepository;
  }

  @GetMapping("/my-reviews")
  public String myReviews(Model model, Principal principal) {
    Long userId = userLookupRepository.findIdByUsername(principal.getName());
    model.addAttribute("reviews", userId == null ? java.util.List.of() : reviewRepository.listForUser(userId, 200));
    return "reviews/my-reviews";
  }
}
