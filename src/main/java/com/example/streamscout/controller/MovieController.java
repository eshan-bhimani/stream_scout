package com.example.streamscout.controller;

import com.example.streamscout.repository.MovieRepository;
import com.example.streamscout.repository.PlatformRepository;
import com.example.streamscout.repository.ReviewRepository;
import com.example.streamscout.repository.UserLookupRepository;
import com.example.streamscout.repository.WatchlistRepository;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MovieController {
  private final MovieRepository movieRepository;
  private final PlatformRepository platformRepository;
  private final ReviewRepository reviewRepository;
  private final WatchlistRepository watchlistRepository;
  private final UserLookupRepository userLookupRepository;

  public MovieController(
      MovieRepository movieRepository,
      PlatformRepository platformRepository,
      ReviewRepository reviewRepository,
      WatchlistRepository watchlistRepository,
      UserLookupRepository userLookupRepository
  ) {
    this.movieRepository = movieRepository;
    this.platformRepository = platformRepository;
    this.reviewRepository = reviewRepository;
    this.watchlistRepository = watchlistRepository;
    this.userLookupRepository = userLookupRepository;
  }

  @GetMapping("/movies")
  public String browse(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String platform,
      @RequestParam(required = false) String genre,
      @RequestParam(required = false) String type,
      @RequestParam(required = false) Integer yearMin,
      @RequestParam(required = false) Integer yearMax,
      Model model
  ) {
    model.addAttribute("platforms", platformRepository.listPlatformNames());
    model.addAttribute("movies", movieRepository.search(
        q, platform, genre, type, yearMin, yearMax, MovieRepository.MAX_BROWSE_RESULTS));
    model.addAttribute("q", q);
    model.addAttribute("platform", platform);
    model.addAttribute("genre", genre);
    model.addAttribute("type", type);
    model.addAttribute("yearMin", yearMin);
    model.addAttribute("yearMax", yearMax);
    return "movies/browse";
  }

  @GetMapping("/movies/{id}")
  public String details(@PathVariable("id") long id, Model model) {
    var details = movieRepository.findDetails(id).orElse(null);
    if (details == null) return "redirect:/movies";
    model.addAttribute("m", details);
    model.addAttribute("reviews", reviewRepository.listForMovie(id, 25));
    return "movies/details";
  }

  @PostMapping("/watchlist/add")
  public String addToWatchlist(@RequestParam("movieId") long movieId, Principal principal) {
    Long userId = userLookupRepository.findIdByUsername(principal.getName());
    if (userId != null) watchlistRepository.add(userId, movieId);
    return "redirect:/movies/" + movieId;
  }

  @PostMapping("/reviews/add")
  public String addReview(
      @RequestParam("movieId") long movieId,
      @RequestParam("stars") int stars,
      @RequestParam(value = "text", required = false) String text,
      Principal principal
  ) {
    Long userId = userLookupRepository.findIdByUsername(principal.getName());
    if (userId != null) reviewRepository.addReview(userId, movieId, stars, text);
    return "redirect:/movies/" + movieId;
  }
}

