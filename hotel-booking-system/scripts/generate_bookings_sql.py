#!/usr/bin/env python3
"""
Build the submission data.sql file from the Kaggle-format CSV (Hotel booking demand).

Writes: users, hotels, room_types (static seed) + bookings (from CSV) + payments (derived).

Default paths (project layout: FinalProject/hotel_bookings.csv, FinalProject/hotel-booking-system/):
  CSV:  ../hotel_bookings.csv  (relative to this repo root)
  OUT: ../data.sql

Rows are split ~half from the start of the file (Resort Hotel) and ~half from City Hotel rows
so both hotels appear in the demo data.

Usage:
  cd hotel-booking-system
  python3 scripts/generate_bookings_sql.py
  python3 scripts/generate_bookings_sql.py ../hotel_bookings.csv data.sql 2500
"""
from __future__ import annotations

import csv
import sys
from datetime import date, datetime, timedelta
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
MONTHS = {
    "January": 1,
    "February": 2,
    "March": 3,
    "April": 4,
    "May": 5,
    "June": 6,
    "July": 7,
    "August": 8,
    "September": 9,
    "October": 10,
    "November": 11,
    "December": 12,
}

STATIC_SQL = """-- =============================================================================
-- data.sql — demo data for Hotel Booking Management & Analytics System
-- Bookings + payments below are generated from Kaggle "Hotel booking demand"
-- (same column layout as jessemostipak/hotel-booking-demand) via:
--   python3 scripts/generate_bookings_sql.py
-- =============================================================================
USE hotel_booking_db;

SET autocommit=0;

-- BCrypt for password: "password" (Spring Security–compatible example hash)
INSERT INTO users (full_name, email, password_hash, role, created_at) VALUES
('System Admin', 'admin@hotel.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'ADMIN', NOW()),
('Alice Customer', 'alice@test.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'CUSTOMER', NOW()),
('Bob Customer', 'bob@test.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'CUSTOMER', NOW()),
('Carol Customer', 'carol@test.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'CUSTOMER', NOW());

INSERT INTO hotels (hotel_name, hotel_type, city, country_code, description) VALUES
('Alpine Resort Hotel', 'Resort', 'Lisbon', 'PRT', 'Resort property aligned to Kaggle H1 "Resort Hotel" series.'),
('Metro City Hotel', 'City', 'Lisbon', 'PRT', 'City property aligned to Kaggle H2 "City Hotel" series.');

INSERT INTO room_types (hotel_id, room_name, max_adults, max_children, bed_type, price_per_night, total_rooms) VALUES
(1, 'Garden Standard', 2, 1, 'Queen', 89.00, 40),
(1, 'Lagoon Deluxe', 2, 2, 'King', 129.00, 25),
(1, 'Family Suite', 4, 2, 'Two Queens', 179.00, 15),
(1, 'Executive Corner', 2, 0, 'King', 159.00, 10),
(1, 'Presidential Villa', 6, 2, 'King + Sofas', 349.00, 5),
(2, 'City Standard', 2, 0, 'Queen', 99.00, 60),
(2, 'Business Plus', 2, 1, 'King', 139.00, 35),
(2, 'Skyline Studio', 2, 2, 'King', 169.00, 20),
(2, 'Executive Twin', 2, 0, 'Two Doubles', 119.00, 30),
(2, 'Penthouse Loft', 4, 2, 'King', 289.00, 8);

"""


def sql_str(s: str | None) -> str:
    if s is None or s == "" or s.upper() == "NULL":
        return "NULL"
    return "'" + str(s).replace("\\", "\\\\").replace("'", "''") + "'"


def room_type_id(hotel: str, letter: str) -> int:
    ch = (letter or "A").strip().upper()[:1] or "A"
    idx = (ord(ch) - ord("A")) % 5
    if hotel.strip() == "Resort Hotel":
        return idx + 1
    return idx + 6


def map_status(is_canceled: str, reservation_status: str) -> tuple[str, int]:
    c = int(float(is_canceled or 0)) == 1
    rs = (reservation_status or "").strip()
    if c or "cancel" in rs.lower():
        return "CANCELED", 1
    if "check-out" in rs.lower() or rs.upper() == "CHECK-OUT":
        return "CHECKED_OUT", 0
    return "BOOKED", 0


def parse_children(val: str) -> int:
    try:
        return int(float(val or 0))
    except ValueError:
        return 0


def row_to_values(row: dict) -> str:
    hotel_csv = row["hotel"].strip()
    hotel_id = 1 if hotel_csv == "Resort Hotel" else 2
    rt_id = room_type_id(hotel_csv, row.get("reserved_room_type") or "A")

    y = int(row["arrival_date_year"])
    m = MONTHS.get(row["arrival_date_month"].strip(), 7)
    d = int(row["arrival_date_day_of_month"])
    arrival = date(y, m, d)
    wknd = int(float(row["stays_in_weekend_nights"] or 0))
    wk = int(float(row["stays_in_week_nights"] or 0))
    nights = max(1, wknd + wk)
    checkout = arrival + timedelta(days=nights)

    status, canceled = map_status(row.get("is_canceled", "0"), row.get("reservation_status", ""))

    adr = float(row.get("adr") or 0)
    meal = row.get("meal") or "BB"
    market = row.get("market_segment") or "Direct"
    dist = row.get("distribution_channel") or "Direct"
    rep = int(float(row.get("is_repeated_guest") or 0)) == 1
    prev_can = int(float(row.get("previous_cancellations") or 0))
    prev_ok = int(float(row.get("previous_bookings_not_canceled") or 0))
    adults = int(float(row.get("adults") or 2))
    babies = int(float(row.get("babies") or 0))
    ch = parse_children(row.get("children", "0"))
    park = int(float(row.get("required_car_parking_spaces") or 0))
    spec = int(float(row.get("total_of_special_requests") or 0))

    booking_date = datetime(y, m, max(1, d - 7))

    vals = (
        "NULL",
        str(hotel_id),
        str(rt_id),
        f"'{booking_date:%Y-%m-%d %H:%M:%S}'",
        f"'{arrival:%Y-%m-%d}'",
        f"'{checkout:%Y-%m-%d}'",
        str(adults),
        str(ch),
        str(babies),
        sql_str(meal),
        sql_str(market),
        sql_str(dist),
        "1" if rep else "0",
        str(prev_can),
        str(prev_ok),
        sql_str(status),
        "1" if canceled else "0",
        str(adr),
        str(spec),
        str(park),
        str(nights),
    )
    return "(" + ", ".join(vals) + ")"


def collect_rows(csv_path: Path, total: int) -> list[dict]:
    n_resort = total // 2
    n_city = total - n_resort
    rows: list[dict] = []

    with csv_path.open(newline="", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for i, row in enumerate(reader):
            if i >= n_resort:
                break
            rows.append(row)

    with csv_path.open(newline="", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        got = 0
        for row in reader:
            if row["hotel"].strip() != "City Hotel":
                continue
            rows.append(row)
            got += 1
            if got >= n_city:
                break

    return rows


def flush_batch(out: list[str], batch: list[str]) -> None:
    if not batch:
        return
    out.append(
        "INSERT INTO bookings (user_id, hotel_id, room_type_id, booking_date, arrival_date, checkout_date, "
        "adults, children, babies, meal_plan, market_segment, distribution_channel, is_repeated_guest, "
        "previous_cancellations, previous_bookings_not_canceled, booking_status, canceled, adr, "
        "special_requests, parking_spaces, total_nights) VALUES\n"
        + ",\n".join(batch)
        + ";\n"
    )
    batch.clear()


def main() -> None:
    csv_path = Path(sys.argv[1]) if len(sys.argv) > 1 else (ROOT.parent / "hotel_bookings.csv")
    out_path = Path(sys.argv[2]) if len(sys.argv) > 2 else (ROOT / "data.sql")
    total = int(sys.argv[3]) if len(sys.argv) > 3 else 2500

    if total < 1001:
        print("Warning: course requires >1000 booking rows; using at least 1001.", file=sys.stderr)
        total = 1001

    if not csv_path.is_file():
        print(f"CSV not found: {csv_path}", file=sys.stderr)
        sys.exit(1)

    parts: list[str] = [STATIC_SQL]
    batch: list[str] = []

    for row in collect_rows(csv_path, total):
        batch.append(row_to_values(row))
        if len(batch) >= 400:
            flush_batch(parts, batch)
    flush_batch(parts, batch)

    parts.append(
        """
INSERT INTO payments (booking_id, amount, payment_method, payment_status, paid_at)
SELECT booking_id,
       ROUND(GREATEST(IFNULL(adr,0), 0.01) * total_nights, 2),
       'CARD',
       CASE WHEN canceled = 1 THEN 'REFUNDED' ELSE 'COMPLETED' END,
       booking_date
FROM bookings
WHERE booking_id NOT IN (SELECT booking_id FROM payments);
"""
    )
    parts.append("COMMIT;\n")

    out_path.write_text("".join(parts), encoding="utf-8")
    print(f"Wrote {out_path} ({total} CSV-derived booking rows + static seed + payments)")


if __name__ == "__main__":
    main()
