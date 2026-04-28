package com.example.hotelbooking.util;

import com.example.hotelbooking.model.User;
import com.example.hotelbooking.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityUtil {

    private final UserRepository userRepository;

    public SecurityUtil(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }
        String email = auth.getName();
        return userRepository.findByEmail(email);
    }

    public int requireUserId() {
        return currentUser().map(User::getUserId)
                .orElseThrow(() -> new IllegalStateException("Not authenticated"));
    }

    public boolean isAdmin() {
        return currentUser().map(u -> "ADMIN".equals(u.getRole())).orElse(false);
    }
}
