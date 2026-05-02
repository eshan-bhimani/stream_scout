package com.example.streamscout.controller;

import com.example.streamscout.repository.UserLookupRepository;
import com.example.streamscout.repository.WatchlistRepository;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WatchlistController {
  private final WatchlistRepository watchlistRepository;
  private final UserLookupRepository userLookupRepository;

  public WatchlistController(WatchlistRepository watchlistRepository, UserLookupRepository userLookupRepository) {
    this.watchlistRepository = watchlistRepository;
    this.userLookupRepository = userLookupRepository;
  }

  @GetMapping("/watchlist")
  public String myWatchlist(Model model, Principal principal) {
    Long userId = userLookupRepository.findIdByUsername(principal.getName());
    model.addAttribute("rows", userId == null ? java.util.List.of() : watchlistRepository.listForUser(userId));
    return "watchlist/watchlist";
  }

  @PostMapping("/watchlist/status")
  public String updateStatus(@RequestParam("id") long id, @RequestParam("status") String status, Principal principal) {
    Long userId = userLookupRepository.findIdByUsername(principal.getName());
    if (userId != null) watchlistRepository.updateStatus(userId, id, status);
    return "redirect:/watchlist";
  }

  @PostMapping("/watchlist/remove")
  public String remove(@RequestParam("id") long id, Principal principal) {
    Long userId = userLookupRepository.findIdByUsername(principal.getName());
    if (userId != null) watchlistRepository.remove(userId, id);
    return "redirect:/watchlist";
  }
}

