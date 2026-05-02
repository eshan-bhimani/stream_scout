-- StreamScout database schema (MySQL 8.x)
-- Domain: OTT movies/series catalog + platform availability + watchlists + reviews

CREATE TABLE IF NOT EXISTS platform (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  UNIQUE KEY uq_platform_name (name)
);

CREATE TABLE IF NOT EXISTS movie (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  content_id VARCHAR(32) NOT NULL,
  title VARCHAR(255) NOT NULL,
  content_type ENUM('Movie','Series') NOT NULL,
  genre VARCHAR(64) NOT NULL,
  country VARCHAR(64) NOT NULL,
  language VARCHAR(64) NOT NULL,
  release_year INT NOT NULL,
  duration_minutes INT NOT NULL,
  rating DECIMAL(3,1) NULL,
  votes INT NULL,
  weighted_rating DECIMAL(4,2) NULL,
  engagement_score DECIMAL(10,2) NULL,
  popularity_score DECIMAL(10,2) NULL,
  trending_score DECIMAL(10,2) NULL,
  tags VARCHAR(255) NULL,
  description TEXT NULL,
  poster_url VARCHAR(512) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_movie_content_id (content_id)
);

-- Many-to-many: movies available on platforms
CREATE TABLE IF NOT EXISTS availability (
  movie_id BIGINT NOT NULL,
  platform_id BIGINT NOT NULL,
  PRIMARY KEY (movie_id, platform_id),
  CONSTRAINT fk_avail_movie FOREIGN KEY (movie_id) REFERENCES movie(id) ON DELETE CASCADE,
  CONSTRAINT fk_avail_platform FOREIGN KEY (platform_id) REFERENCES platform(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS app_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(32) NOT NULL,
  password_hash VARCHAR(72) NOT NULL,
  role ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER',
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_user_username (username)
);

CREATE TABLE IF NOT EXISTS watchlist (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  movie_id BIGINT NOT NULL,
  status ENUM('PLANNED','WATCHING','COMPLETED','DROPPED') NOT NULL DEFAULT 'PLANNED',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_watchlist_user_movie (user_id, movie_id),
  CONSTRAINT fk_watch_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
  CONSTRAINT fk_watch_movie FOREIGN KEY (movie_id) REFERENCES movie(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS review (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  movie_id BIGINT NOT NULL,
  stars TINYINT NOT NULL,
  review_text VARCHAR(1000) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT chk_review_stars CHECK (stars BETWEEN 1 AND 5),
  CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
  CONSTRAINT fk_review_movie FOREIGN KEY (movie_id) REFERENCES movie(id) ON DELETE CASCADE
);

-- Indexes (to satisfy "at least two indexes to optimize queries"):
-- 1) Searching/browsing by title + year filters
CREATE INDEX idx_movie_title_year ON movie(title, release_year);
-- 2) Aggregations by platform / join availability
CREATE INDEX idx_availability_platform_movie ON availability(platform_id, movie_id);
-- Helpful for movie review aggregation pages
CREATE INDEX idx_review_movie_created ON review(movie_id, created_at);

