-- StreamScout: SQL statements used by the application (via prepared statements)

-- Q1 (Browse/search with joins and filters)
-- MovieRepository.search(...)
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
LIMIT ?;

-- Q2 (Movie details + aggregation of reviews)
-- MovieRepository.findDetails(...)
SELECT
  m.id, m.content_id, m.title, m.content_type, m.genre, m.country, m.language,
  m.release_year, m.duration_minutes, m.rating, m.votes, m.weighted_rating,
  m.engagement_score, m.popularity_score, m.trending_score, m.tags, m.description, m.poster_url,
  AVG(r.stars) AS avg_stars,
  COUNT(r.id) AS review_count
FROM movie m
LEFT JOIN review r ON r.movie_id = m.id
WHERE m.id = ?
GROUP BY m.id;

-- Q3 (Platform stats: join + group-by)
-- PlatformRepository.getPlatformStats(...)
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
ORDER BY title_count DESC, avg_trending DESC;

-- Q4 (Add to watchlist - insertion)
-- WatchlistRepository.add(...)
INSERT INTO watchlist (user_id, movie_id, status, created_at)
VALUES (?, ?, 'PLANNED', NOW())
ON DUPLICATE KEY UPDATE created_at = created_at;

-- Q5 (Update watchlist status - update)
-- WatchlistRepository.updateStatus(...)
UPDATE watchlist SET status = ? WHERE id = ? AND user_id = ?;

-- Q6 (Remove from watchlist - deletion)
-- WatchlistRepository.remove(...)
DELETE FROM watchlist WHERE id = ? AND user_id = ?;

-- Q7 (Insert a review)
-- ReviewRepository.addReview(...)
INSERT INTO review (user_id, movie_id, stars, review_text, created_at)
VALUES (?, ?, ?, ?, NOW());

-- Q8 (List reviews for movie - join)
-- ReviewRepository.listForMovie(...)
SELECT r.id, u.username, r.stars, r.review_text, r.created_at
FROM review r
JOIN app_user u ON u.id = r.user_id
WHERE r.movie_id = ?
ORDER BY r.created_at DESC
LIMIT ?;

