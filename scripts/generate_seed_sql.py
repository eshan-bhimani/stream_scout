import csv
import re
from pathlib import Path


def sql_escape(s: str) -> str:
    return s.replace("\\", "\\\\").replace("'", "''")


def norm_platform(name: str) -> str:
    name = (name or "").strip()
    return re.sub(r"\s+", " ", name)


def main() -> None:
    csv_path = Path("/Users/lohithmanthena/Downloads/ott_movies_clean_unique.csv")
    out_path = Path(__file__).resolve().parents[1] / "db" / "seed.sql"

    platforms: dict[str, int] = {}
    movies: list[dict] = []
    avail: list[tuple[int, int]] = []

    with csv_path.open(newline="", encoding="utf-8") as f:
        r = csv.DictReader(f)
        for row in r:
            platform = norm_platform(row.get("platform", ""))
            if platform not in platforms:
                platforms[platform] = len(platforms) + 1

            movies.append(
                {
                    "content_id": row.get("content_id", "").strip(),
                    "title": row.get("title", "").strip(),
                    "content_type": row.get("type", "").strip(),
                    "genre": row.get("genre", "").strip(),
                    "country": row.get("country", "").strip(),
                    "language": row.get("language", "").strip(),
                    "release_year": int(row.get("release_year") or 0),
                    "duration_minutes": int(float(row.get("duration_minutes") or 0)),
                    "rating": row.get("rating", "").strip(),
                    "votes": row.get("votes", "").strip(),
                    "weighted_rating": row.get("weighted_rating", "").strip(),
                    "engagement_score": row.get("engagement_score", "").strip(),
                    "popularity_score": row.get("popularity_score", "").strip(),
                    "trending_score": row.get("trending_score", "").strip(),
                    "tags": row.get("tags", "").strip(),
                    "description": row.get("description", "").strip(),
                    "poster_url": row.get("poster_url", "").strip(),
                    "platform": platform,
                }
            )

    # Deterministic seed: platforms then movies by input order
    with out_path.open("w", encoding="utf-8") as out:
        out.write("-- Seed data for StreamScout\n")
        out.write("-- Generated from ott_movies_clean_unique.csv\n\n")

        out.write("INSERT INTO platform (id, name) VALUES\n")
        out.write(
            ",\n".join(
                f"({pid}, '{sql_escape(pname)}')"
                for pname, pid in sorted(platforms.items(), key=lambda x: x[1])
            )
        )
        out.write(";\n\n")

        out.write("-- Movies\n")
        out.write(
            "INSERT INTO movie (content_id, title, content_type, genre, country, language, release_year, duration_minutes, "
            "rating, votes, weighted_rating, engagement_score, popularity_score, trending_score, tags, description, poster_url)\nVALUES\n"
        )
        movie_rows = []
        for m in movies:
            def n(v: str):
                v = (v or "").strip()
                return "NULL" if v == "" else v

            movie_rows.append(
                "("
                + ", ".join(
                    [
                        f"'{sql_escape(m['content_id'])}'",
                        f"'{sql_escape(m['title'])}'",
                        f"'{sql_escape(m['content_type'])}'",
                        f"'{sql_escape(m['genre'])}'",
                        f"'{sql_escape(m['country'])}'",
                        f"'{sql_escape(m['language'])}'",
                        str(m["release_year"]),
                        str(m["duration_minutes"]),
                        n(m["rating"]),
                        n(m["votes"]),
                        n(m["weighted_rating"]),
                        n(m["engagement_score"]),
                        n(m["popularity_score"]),
                        n(m["trending_score"]),
                        ("NULL" if m["tags"] == "" else f"'{sql_escape(m['tags'])}'"),
                        ("NULL" if m["description"] == "" else f"'{sql_escape(m['description'])}'"),
                        ("NULL" if m["poster_url"] == "" else f"'{sql_escape(m['poster_url'])}'"),
                    ]
                )
                + ")"
            )
        out.write(",\n".join(movie_rows))
        out.write(";\n\n")

        out.write("-- Availability (join movie + platform using content_id mapping)\n")
        out.write(
            "INSERT INTO availability (movie_id, platform_id)\n"
            "SELECT m.id, p.id\n"
            "FROM movie m\n"
            "JOIN (\n"
        )

        # Create inline mapping table for content_id -> platform_name
        mapping_rows = []
        for m in movies:
            mapping_rows.append(
                f"SELECT '{sql_escape(m['content_id'])}' AS content_id, '{sql_escape(m['platform'])}' AS platform_name"
            )
        out.write("\nUNION ALL\n".join(mapping_rows))
        out.write("\n) x ON x.content_id = m.content_id\nJOIN platform p ON p.name = x.platform_name;\n")

    print(f"Wrote {out_path}")


if __name__ == "__main__":
    main()

