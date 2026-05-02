package com.example.streamscout.repository;

import com.example.streamscout.model.MovieCard;
import com.example.streamscout.model.MovieDetails;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

@Repository
public class MovieRepository {
  /** Upper bound for browse/search pages (dataset is ~2500 rows; leave headroom). */
  public static final int MAX_BROWSE_RESULTS = 10_000;

  private final DataSource dataSource;

  public MovieRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public List<MovieCard> search(String q, String platform, String genre, String type, Integer yearMin, Integer yearMax, int limit) {
    String sql = """
        SELECT m.id, m.content_id, m.title, m.content_type, m.genre, m.release_year, m.duration_minutes, m.rating, m.trending_score, m.poster_url
        FROM movie m
        JOIN availability a ON a.movie_id = m.id
        JOIN platform p ON p.id = a.platform_id
        WHERE (? IS NULL OR m.title LIKE CONCAT('%', ?, '%'))
          AND (? IS NULL OR p.name = ?)
          AND (? IS NULL OR m.genre = ?)
          AND (? IS NULL OR m.content_type = ?)
          AND (? IS NULL OR m.release_year >= ?)
          AND (? IS NULL OR m.release_year <= ?)
        GROUP BY m.id
        ORDER BY m.trending_score DESC, m.weighted_rating DESC, m.votes DESC
        LIMIT ?
        """;

    try (Connection conn = dataSource.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      int i = 1;
      ps.setString(i++, emptyToNull(q));
      ps.setString(i++, emptyToNull(q));
      ps.setString(i++, emptyToNull(platform));
      ps.setString(i++, emptyToNull(platform));
      ps.setString(i++, emptyToNull(genre));
      ps.setString(i++, emptyToNull(genre));
      ps.setString(i++, emptyToNull(type));
      ps.setString(i++, emptyToNull(type));
      ps.setObject(i++, yearMin);
      ps.setObject(i++, yearMin);
      ps.setObject(i++, yearMax);
      ps.setObject(i++, yearMax);
      ps.setInt(i, Math.max(1, Math.min(limit, MAX_BROWSE_RESULTS)));

      List<MovieCard> out = new ArrayList<>();
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          out.add(new MovieCard(
              rs.getLong("id"),
              rs.getString("content_id"),
              rs.getString("title"),
              rs.getString("content_type"),
              rs.getString("genre"),
              rs.getInt("release_year"),
              rs.getInt("duration_minutes"),
              (Double) rs.getObject("rating", Double.class),
              (Double) rs.getObject("trending_score", Double.class),
              rs.getString("poster_url")
          ));
        }
      }
      return out;
    } catch (SQLException e) {
      throw new RuntimeException("Failed to search movies", e);
    }
  }

  public Optional<MovieDetails> findDetails(long movieId) {
    String coreSql = """
        SELECT
          m.id, m.content_id, m.title, m.content_type, m.genre, m.country, m.language,
          m.release_year, m.duration_minutes, m.rating, m.votes, m.weighted_rating,
          m.engagement_score, m.popularity_score, m.trending_score, m.tags, m.description, m.poster_url,
          AVG(r.stars) AS avg_stars,
          COUNT(r.id) AS review_count
        FROM movie m
        LEFT JOIN review r ON r.movie_id = m.id
        WHERE m.id = ?
        GROUP BY m.id
        """;
    String platSql = """
        SELECT p.name
        FROM availability a
        JOIN platform p ON p.id = a.platform_id
        WHERE a.movie_id = ?
        ORDER BY p.name
        """;

    try (Connection conn = dataSource.getConnection();
         PreparedStatement corePs = conn.prepareStatement(coreSql);
         PreparedStatement platPs = conn.prepareStatement(platSql)) {
      corePs.setLong(1, movieId);
      MovieDetails core;
      try (ResultSet rs = corePs.executeQuery()) {
        if (!rs.next()) return Optional.empty();
        core = new MovieDetails(
            rs.getLong("id"),
            rs.getString("content_id"),
            rs.getString("title"),
            rs.getString("content_type"),
            rs.getString("genre"),
            rs.getString("country"),
            rs.getString("language"),
            rs.getInt("release_year"),
            rs.getInt("duration_minutes"),
            (Double) rs.getObject("rating", Double.class),
            (Integer) rs.getObject("votes", Integer.class),
            (Double) rs.getObject("weighted_rating", Double.class),
            (Double) rs.getObject("engagement_score", Double.class),
            (Double) rs.getObject("popularity_score", Double.class),
            (Double) rs.getObject("trending_score", Double.class),
            rs.getString("tags"),
            rs.getString("description"),
            rs.getString("poster_url"),
            List.of(),
            (Double) rs.getObject("avg_stars", Double.class),
            rs.getInt("review_count")
        );
      }

      platPs.setLong(1, movieId);
      List<String> platforms = new ArrayList<>();
      try (ResultSet rs = platPs.executeQuery()) {
        while (rs.next()) platforms.add(rs.getString("name"));
      }

      return Optional.of(new MovieDetails(
          core.id(), core.contentId(), core.title(), core.contentType(), core.genre(), core.country(), core.language(),
          core.releaseYear(), core.durationMinutes(), core.rating(), core.votes(), core.weightedRating(),
          core.engagementScore(), core.popularityScore(), core.trendingScore(), core.tags(), core.description(), core.posterUrl(),
          platforms, core.avgStars(), core.reviewCount()
      ));
    } catch (SQLException e) {
      throw new RuntimeException("Failed to load movie details", e);
    }
  }

  private static String emptyToNull(String s) {
    if (s == null) return null;
    String t = s.trim();
    return t.isEmpty() ? null : t;
  }
}

