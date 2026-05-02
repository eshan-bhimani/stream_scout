package com.example.streamscout.repository;

import com.example.streamscout.model.PlatformStats;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

@Repository
public class PlatformRepository {
  private final DataSource dataSource;

  public PlatformRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public List<String> listPlatformNames() {
    String sql = "SELECT name FROM platform ORDER BY name";
    try (Connection conn = dataSource.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
      List<String> out = new ArrayList<>();
      while (rs.next()) out.add(rs.getString("name"));
      return out;
    } catch (SQLException e) {
      throw new RuntimeException("Failed to list platforms", e);
    }
  }

  public List<PlatformStats> getPlatformStats() {
    // Join + aggregation query (non-trivial)
    String sql = """
        SELECT
          p.id AS platform_id,
          p.name AS platform_name,
          COUNT(*) AS title_count,
          COALESCE(AVG(m.rating), 0) AS avg_rating,
          COALESCE(AVG(m.trending_score), 0) AS avg_trending
        FROM platform p
        JOIN availability a ON a.platform_id = p.id
        JOIN movie m ON m.id = a.movie_id
        GROUP BY p.id
        ORDER BY title_count DESC, avg_trending DESC
        """;
    try (Connection conn = dataSource.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
      List<PlatformStats> out = new ArrayList<>();
      while (rs.next()) {
        out.add(new PlatformStats(
            rs.getLong("platform_id"),
            rs.getString("platform_name"),
            rs.getLong("title_count"),
            rs.getDouble("avg_rating"),
            rs.getDouble("avg_trending")
        ));
      }
      return out;
    } catch (SQLException e) {
      throw new RuntimeException("Failed to load platform stats", e);
    }
  }
}

