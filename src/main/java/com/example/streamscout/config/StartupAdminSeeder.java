package com.example.streamscout.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class StartupAdminSeeder implements ApplicationRunner {
  private final DataSource dataSource;
  private final PasswordEncoder passwordEncoder;

  public StartupAdminSeeder(DataSource dataSource, PasswordEncoder passwordEncoder) {
    this.dataSource = dataSource;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(ApplicationArguments args) {
    // Creates a default admin if none exists. Safe to re-run.
    // Credentials: admin / admin12345
    if (adminExists()) return;
    String sql = """
        INSERT INTO app_user (username, password_hash, role, enabled, created_at)
        VALUES (?, ?, 'ADMIN', 1, NOW())
        """;
    try (Connection conn = dataSource.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, "admin");
      ps.setString(2, passwordEncoder.encode("admin12345"));
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to seed default admin", e);
    }
  }

  private boolean adminExists() {
    String sql = "SELECT 1 FROM app_user WHERE role='ADMIN' LIMIT 1";
    try (Connection conn = dataSource.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
      return rs.next();
    } catch (SQLException e) {
      // If DB isn't ready (e.g., first boot before docker), don't fail app startup.
      return false;
    }
  }
}

