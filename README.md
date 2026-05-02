# StreamScout — OTT Catalog, Watchlist & Reviews (Spring Boot + MySQL)

## Title
**StreamScout: OTT Movies/Series Discovery with Watchlists and Reviews**

## Problem & domain
With content split across multiple OTT platforms, users waste time figuring out **what’s available where**, tracking what they want to watch, and comparing community feedback. The domain is **online streaming catalogs** (movies/series), platform availability, and user-generated lists/reviews.

## Solution (what this application does)
StreamScout is a Spring Boot web application backed by a MySQL database that:
- lets users **browse/search** a catalog of movies/series
- shows **which platform(s)** (Netflix / Prime Video / Hotstar) a title is available on
- supports a personal **watchlist** with statuses (planned/watching/completed/dropped)
- supports **reviews** (1–5 stars + optional text)
- provides **platform analytics** (join + aggregation queries)

### User interfaces (5+)
- **Home** (`/`): trending picks from the database
- **Browse/Search** (`/movies`): filter by title/platform/type/year and list results
- **Movie details** (`/movies/{id}`): availability + review summary + recent reviews
- **My watchlist** (`/watchlist`): list, update status, remove items (CRUD)
- **Platforms analytics** (`/platforms`): platform-level aggregations via join/group-by
- **Auth pages** (`/auth/login`, `/auth/signup`): signup/login

## Technologies
- **Java 21**, **Spring Boot 3**, Maven
- **Spring Security** (form login) + **BCrypt** password hashing
- **JDBC** (`spring-boot-starter-jdbc`) with **prepared statements**
- **MySQL 8.4** in Docker Compose
- Thymeleaf templates + simple CSS

## Database design deliverables
- ER + normalization writeup: `docs/db_design.md`
- PDF export (submission-friendly): `docs/db_design.pdf` (regenerate via script below)
- Schema + constraints + indexes: `db/schema.sql`
- Seed data generated from CSV: `db/seed.sql` (created by `scripts/generate_seed_sql.py`)
- Query list used by the app: `db/queries.sql`

Regenerate the PDF from markdown (uses a local venv; does not touch system Python):

```bash
python3 -m venv scripts/.venv-pdf
./scripts/.venv-pdf/bin/python -m pip install fpdf2
./scripts/.venv-pdf/bin/python scripts/generate_db_design_pdf.py
```

## Dataset
Seed data comes from: `ott_movies_clean_unique.csv` (2500 rows).  
The application seeds:
- `movie`: **2500 rows** (requirement: at least one table > 1000 rows)
- `platform`: 3 rows
- `availability`: 2500 rows

## How to run
Start the database:

```bash
docker compose up -d
```

Run the web app:

```bash
mvn spring-boot:run
```

Open:
- `http://localhost:9080/` (default; override with env var `SERVER_PORT`)

If you hit “address already in use”, pick another port:

```bash
SERVER_PORT=9091 mvn spring-boot:run
```

To see what is holding a port (example: 9080):

```bash
lsof -nP -iTCP:9080 -sTCP:LISTEN
```

### Default admin
On first successful app startup (with DB reachable), a default admin is created:
- username: `admin`
- password: `admin12345`

## Notes
- DB runs on host port **3307** (container 3306) to avoid conflicts.
- This project uses JDBC prepared statements for all SQL executed from Java.

## IDE note (Cursor/VS Code)
If Java files show “non-project file, only syntax errors are reported”, open the repo root as a workspace folder and import it as a **Maven** project (so `pom.xml` is recognized). This is an editor configuration issue, not a compile error—`mvn package` should still work.

