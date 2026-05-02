package com.example.streamscout.repository;

import com.example.streamscout.model.AppUser;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
  private final DataSource dataSource;

  public UserRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public Optional<AppUser> findByUsername(String username) {
    String sql = """
        SELECT id, username, password_hash, role, enabled
        FROM app_user
        WHERE username = ?
        """;
    try (Connection conn = dataSource.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, username);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return Optional.empty();
        return Optional.of(new AppUser(
            rs.getLong("id"),
            rs.getString("username"),
            rs.getString("password_hash"),
            rs.getString("role"),
            rs.getBoolean("enabled")
        ));
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to find user", e);
    }
  }

  public boolean usernameExists(String username) {
    String sql = "SELECT 1 FROM app_user WHERE username = ? LIMIT 1";
    try (Connection conn = dataSource.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, username);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to check username", e);
    }
  }

  public long createUser(String username, String passwordHash, String role) {
    String sql = """
        INSERT INTO app_user (username, password_hash, role, enabled, created_at)
        VALUES (?, ?, ?, 1, NOW())
        """;
    try (Connection conn = dataSource.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, username);
      ps.setString(2, passwordHash);
      ps.setString(3, role);
      ps.executeUpdate();
      try (ResultSet keys = ps.getGeneratedKeys()) {
        if (!keys.next()) throw new SQLException("No generated key returned");
        return keys.getLong(1);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to create user", e);
    }
  }
}

