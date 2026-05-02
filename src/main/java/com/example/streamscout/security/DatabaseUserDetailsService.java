package com.example.streamscout.security;

import com.example.streamscout.repository.UserRepository;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
  private final UserRepository userRepository;

  public DatabaseUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userRepository.findByUsername(username)
        .map(u -> new User(
            u.username(),
            u.passwordHash(),
            u.enabled(),
            true,
            true,
            true,
            List.of(new SimpleGrantedAuthority("ROLE_" + u.role()))
        ))
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
  }
}

