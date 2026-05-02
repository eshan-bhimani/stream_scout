package com.example.streamscout.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

@Repository
public class UserLookupRepository {
  private final DataSource dataSource;

  public UserLookupRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public Long findIdByUsername(String username) {
    String sql = "SELECT id FROM app_user WHERE username = ? LIMIT 1";
    try (Connection conn = dataSource.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, username);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return null;
        return rs.getLong("id");
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to lookup user id", e);
    }
  }
}

