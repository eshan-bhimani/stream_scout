package com.example.streamscout.repository;

import com.example.streamscout.model.ReviewRow;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

@Repository
public class ReviewRepository {
  private final DataSource dataSource;

  public ReviewRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public List<ReviewRow> listForMovie(long movieId, int limit) {
    // Join query: review + user
    String sql = """
        SELECT r.id, u.username, r.stars, r.review_text, r.created_at
        FROM review r
        JOIN app_user u ON u.id = r.user_id
        WHERE r.movie_id = ?
        ORDER BY r.created_at DESC
        LIMIT ?
        """;
    try (Connection conn = dataSource.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, movieId);
      ps.setInt(2, Math.max(1, Math.min(limit, 200)));
      List<ReviewRow> out = new ArrayList<>();
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          out.add(new ReviewRow(
              rs.getLong("id"),
              rs.getString("username"),
              rs.getInt("stars"),
              rs.getString("review_text"),
              rs.getTimestamp("created_at").toInstant()
          ));
        }
      }
      return out;
    } catch (SQLException e) {
      throw new RuntimeException("Failed to list reviews", e);
    }
  }

  public void addReview(long userId, long movieId, int stars, String text) {
    String sql = """
        INSERT INTO review (user_id, movie_id, stars, review_text, created_at)
        VALUES (?, ?, ?, ?, NOW())
        """;
    try (Connection conn = dataSource.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, userId);
      ps.setLong(2, movieId);
      ps.setInt(3, stars);
      ps.setString(4, (text == null || text.isBlank()) ? null : text.trim());
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to add review", e);
    }
  }
}

