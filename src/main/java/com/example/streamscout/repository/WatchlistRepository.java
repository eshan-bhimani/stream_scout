package com.example.streamscout.repository;

import com.example.streamscout.model.WatchlistRow;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

@Repository
public class WatchlistRepository {
  private final DataSource dataSource;

  public WatchlistRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public List<WatchlistRow> listForUser(long userId) {
    // Join query: watchlist + movie
    String sql = """
        SELECT w.id, m.id AS movie_id, m.title, m.content_type, m.release_year, w.status, w.created_at
        FROM watchlist w
        JOIN movie m ON m.id = w.movie_id
        WHERE w.user_id = ?
        ORDER BY w.created_at DESC
        """;
    try (Connection conn = dataSource.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, userId);
      List<WatchlistRow> out = new ArrayList<>();
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          out.add(new WatchlistRow(
              rs.getLong("id"),
              rs.getLong("movie_id"),
              rs.getString("title"),
              rs.getString("content_type"),
              rs.getInt("release_year"),
              rs.getString("status"),
              rs.getTimestamp("created_at").toInstant()
          ));
        }
      }
      return out;
    } catch (SQLException e) {
      throw new RuntimeException("Failed to list watchlist", e);
    }
  }

  public void add(long userId, long movieId) {
    String sql = """
        INSERT INTO watchlist (user_id, movie_id, status, created_at)
        VALUES (?, ?, 'PLANNED', NOW())
        ON DUPLICATE KEY UPDATE created_at = created_at
        """;
    try (Connection conn = dataSource.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, userId);
      ps.setLong(2, movieId);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to add to watchlist", e);
    }
  }

  public void updateStatus(long userId, long watchlistId, String status) {
    String sql = "UPDATE watchlist SET status = ? WHERE id = ? AND user_id = ?";
    try (Connection conn = dataSource.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, status);
      ps.setLong(2, watchlistId);
      ps.setLong(3, userId);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to update watchlist", e);
    }
  }

  public void remove(long userId, long watchlistId) {
    String sql = "DELETE FROM watchlist WHERE id = ? AND user_id = ?";
    try (Connection conn = dataSource.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, watchlistId);
      ps.setLong(2, userId);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to remove from watchlist", e);
    }
  }
}

