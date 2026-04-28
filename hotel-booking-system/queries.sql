-- =============================================================================
-- queries.sql — SQL used by the Hotel Booking app (placeholders = prepared ?)
-- Each statement is executed via JdbcTemplate with PreparedStatement parameters.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- INSERT: Register new customer after signup form
-- Page: POST /signup  (AuthController → AuthService → UserRepository.insert)
-- ---------------------------------------------------------------------------
-- INSERT INTO users (full_name, email, password_hash, role, created_at)
-- VALUES (?, ?, ?, 'CUSTOMER', NOW());

-- ---------------------------------------------------------------------------
-- SELECT: Load user by email for Spring Security login
-- Page: POST /login  (DatabaseUserDetailsService.loadUserByUsername)
-- ---------------------------------------------------------------------------
-- SELECT user_id, full_name, email, password_hash, role, created_at
-- FROM users WHERE email = ?;

-- ---------------------------------------------------------------------------
-- SELECT: Check duplicate email on signup
-- Page: POST /signup
-- ---------------------------------------------------------------------------
-- SELECT COUNT(*) FROM users WHERE email = ?;

-- ---------------------------------------------------------------------------
-- SELECT: List hotels for home, hotels page, search dropdown, admin filters
-- Pages: GET /, GET /hotels, GET /bookings/search (model), GET /admin/bookings, etc.
-- ---------------------------------------------------------------------------
-- SELECT hotel_id, hotel_name, hotel_type, city, country_code, description
-- FROM hotels ORDER BY hotel_name;

-- ---------------------------------------------------------------------------
-- SELECT: One hotel for details and search header
-- Pages: GET /hotels/{id}, GET /bookings/search
-- ---------------------------------------------------------------------------
-- SELECT hotel_id, hotel_name, hotel_type, city, country_code, description
-- FROM hotels WHERE hotel_id = ?;

-- ---------------------------------------------------------------------------
-- SELECT: Room types for a hotel (details + availability base set)
-- Pages: GET /hotels/{id}, (availability query joins this table)
-- ---------------------------------------------------------------------------
-- SELECT room_type_id, hotel_id, room_name, max_adults, max_children, bed_type,
--        price_per_night, total_rooms
-- FROM room_types WHERE hotel_id = ? ORDER BY room_name;

-- ---------------------------------------------------------------------------
-- SELECT: All room types for admin room list
-- Page: GET /admin/rooms
-- ---------------------------------------------------------------------------
-- SELECT room_type_id, hotel_id, room_name, max_adults, max_children, bed_type,
--        price_per_night, total_rooms
-- FROM room_types ORDER BY hotel_id, room_name;

-- ---------------------------------------------------------------------------
-- SELECT: One room type (booking flow + edit form)
-- Pages: POST /bookings (create), GET /admin/rooms/edit
-- ---------------------------------------------------------------------------
-- SELECT room_type_id, hotel_id, room_name, max_adults, max_children, bed_type,
--        price_per_night, total_rooms
-- FROM room_types WHERE room_type_id = ?;

-- ---------------------------------------------------------------------------
-- SELECT + JOIN + GROUP BY: Room availability vs overlapping active bookings
-- Page: GET /bookings/search
-- ---------------------------------------------------------------------------
-- SELECT rt.room_type_id, rt.room_name, rt.total_rooms, rt.max_adults, rt.max_children,
--        rt.bed_type, rt.price_per_night,
--        COUNT(b.booking_id) AS booked_count
-- FROM room_types rt
-- LEFT JOIN bookings b
--   ON rt.room_type_id = b.room_type_id
--  AND b.canceled = 0
--  AND b.arrival_date < ?
--  AND b.checkout_date > ?
-- WHERE rt.hotel_id = ?
-- GROUP BY rt.room_type_id, rt.room_name, rt.total_rooms, rt.max_adults, rt.max_children,
--          rt.bed_type, rt.price_per_night
-- ORDER BY rt.room_name;

-- ---------------------------------------------------------------------------
-- INSERT: New customer booking + payment row
-- Pages: POST /bookings  (BookingService → BookingRepository.insert, PaymentRepository.insert)
-- ---------------------------------------------------------------------------
-- INSERT INTO bookings (
--   user_id, hotel_id, room_type_id, booking_date, arrival_date, checkout_date,
--   adults, children, babies, meal_plan, market_segment, distribution_channel,
--   is_repeated_guest, previous_cancellations, previous_bookings_not_canceled,
--   booking_status, canceled, adr, special_requests, parking_spaces, total_nights
-- ) VALUES (?, ?, ?, NOW(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
--
-- INSERT INTO payments (booking_id, amount, payment_method, payment_status, paid_at)
-- VALUES (?, ?, ?, ?, NOW());

-- ---------------------------------------------------------------------------
-- SELECT + JOIN: Current user booking history
-- Page: GET /bookings/my
-- ---------------------------------------------------------------------------
-- SELECT b.booking_id, h.hotel_name, r.room_name, b.arrival_date, b.checkout_date,
--        b.booking_status, b.canceled, b.adr
-- FROM bookings b
-- JOIN hotels h ON b.hotel_id = h.hotel_id
-- JOIN room_types r ON b.room_type_id = r.room_type_id
-- WHERE b.user_id = ?
-- ORDER BY b.arrival_date DESC;

-- ---------------------------------------------------------------------------
-- SELECT: Single booking (detail / edit / ownership check)
-- Pages: GET /bookings/{id}, GET /bookings/{id}/edit
-- ---------------------------------------------------------------------------
-- SELECT booking_id, user_id, hotel_id, room_type_id, booking_date, arrival_date, checkout_date,
--        adults, children, babies, meal_plan, market_segment, distribution_channel,
--        is_repeated_guest, previous_cancellations, previous_bookings_not_canceled,
--        booking_status, canceled, adr, special_requests, parking_spaces, total_nights
-- FROM bookings WHERE booking_id = ?;

-- ---------------------------------------------------------------------------
-- UPDATE: Customer cancel
-- Page: POST /bookings/{id}/cancel
-- ---------------------------------------------------------------------------
-- UPDATE bookings
-- SET canceled = 1, booking_status = 'CANCELED'
-- WHERE booking_id = ? AND user_id = ?;

-- ---------------------------------------------------------------------------
-- UPDATE: Customer edit stay / party
-- Page: POST /bookings/{id}/update
-- ---------------------------------------------------------------------------
-- UPDATE bookings
-- SET arrival_date = ?, checkout_date = ?, adults = ?, children = ?, babies = ?, meal_plan = ?,
--     total_nights = ?
-- WHERE booking_id = ? AND user_id = ? AND canceled = 0;

-- ---------------------------------------------------------------------------
-- SELECT + JOIN + optional filters: Admin booking grid
-- Page: GET /admin/bookings
-- ---------------------------------------------------------------------------
-- SELECT b.booking_id, b.user_id, u.email AS user_email, h.hotel_name, r.room_name,
--        b.booking_date, b.arrival_date, b.checkout_date, b.booking_status, b.canceled, b.adr
-- FROM bookings b
-- JOIN hotels h ON b.hotel_id = h.hotel_id
-- JOIN room_types r ON b.room_type_id = r.room_type_id
-- LEFT JOIN users u ON b.user_id = u.user_id
-- WHERE 1=1  [AND b.hotel_id = ?]  [AND b.booking_status = ?]  [AND b.canceled = 1]
-- ORDER BY b.arrival_date DESC LIMIT 500;

-- ---------------------------------------------------------------------------
-- UPDATE: Admin change status / canceled flag
-- Page: POST /admin/bookings/status
-- ---------------------------------------------------------------------------
-- UPDATE bookings SET booking_status = ?, canceled = ? WHERE booking_id = ?;

-- ---------------------------------------------------------------------------
-- DELETE: Admin remove payments for a booking (FK child first)
-- Page: POST /admin/bookings/delete
-- ---------------------------------------------------------------------------
-- DELETE FROM payments WHERE booking_id = ?;

-- ---------------------------------------------------------------------------
-- DELETE: Admin remove booking row
-- Page: POST /admin/bookings/delete
-- ---------------------------------------------------------------------------
-- DELETE FROM bookings WHERE booking_id = ?;

-- ---------------------------------------------------------------------------
-- SELECT + aggregation: Dashboard total bookings
-- Page: GET /admin
-- ---------------------------------------------------------------------------
-- SELECT COUNT(*) FROM bookings;

-- ---------------------------------------------------------------------------
-- SELECT + aggregation: Dashboard canceled count
-- Page: GET /admin
-- ---------------------------------------------------------------------------
-- SELECT COUNT(*) FROM bookings WHERE canceled = 1;

-- ---------------------------------------------------------------------------
-- SELECT + aggregation: Dashboard average ADR
-- Page: GET /admin
-- ---------------------------------------------------------------------------
-- SELECT AVG(adr) FROM bookings WHERE adr IS NOT NULL AND adr > 0;

-- ---------------------------------------------------------------------------
-- SELECT + aggregation: Revenue from completed payments
-- Page: GET /admin
-- ---------------------------------------------------------------------------
-- SELECT COALESCE(SUM(amount), 0) FROM payments WHERE payment_status = 'COMPLETED';

-- ---------------------------------------------------------------------------
-- SELECT + JOIN + GROUP BY + aggregates: Analytics by hotel
-- Page: GET /admin (dashboard table)
-- ---------------------------------------------------------------------------
-- SELECT h.hotel_name,
--        COUNT(*) AS total_bookings,
--        SUM(CASE WHEN b.canceled = 1 THEN 1 ELSE 0 END) AS canceled_bookings,
--        ROUND(AVG(b.adr), 2) AS avg_daily_rate
-- FROM bookings b
-- JOIN hotels h ON b.hotel_id = h.hotel_id
-- GROUP BY h.hotel_id, h.hotel_name
-- ORDER BY total_bookings DESC;

-- ---------------------------------------------------------------------------
-- SELECT + GROUP BY: Booking volume by arrival month
-- Page: GET /admin (dashboard)
-- ---------------------------------------------------------------------------
-- SELECT YEAR(arrival_date) AS yr, MONTH(arrival_date) AS mn, COUNT(*) AS total_bookings
-- FROM bookings
-- GROUP BY YEAR(arrival_date), MONTH(arrival_date)
-- ORDER BY yr, mn;

-- ---------------------------------------------------------------------------
-- SELECT + JOIN + GROUP BY: Room usage / average stay
-- Page: GET /admin (dashboard)
-- ---------------------------------------------------------------------------
-- SELECT r.room_name, h.hotel_name,
--        COUNT(*) AS total_bookings,
--        ROUND(AVG(b.total_nights), 2) AS avg_stay_length
-- FROM bookings b
-- JOIN room_types r ON b.room_type_id = r.room_type_id
-- JOIN hotels h ON b.hotel_id = h.hotel_id
-- GROUP BY r.room_type_id, r.room_name, h.hotel_name
-- ORDER BY total_bookings DESC;

-- ---------------------------------------------------------------------------
-- INSERT / UPDATE: Admin hotel save (insert if no id, else update)
-- Pages: POST /admin/hotels/save
-- ---------------------------------------------------------------------------
-- INSERT INTO hotels (hotel_name, hotel_type, city, country_code, description)
-- VALUES (?, ?, ?, ?, ?);
--
-- UPDATE hotels
-- SET hotel_name = ?, hotel_type = ?, city = ?, country_code = ?, description = ?
-- WHERE hotel_id = ?;

-- ---------------------------------------------------------------------------
-- INSERT / UPDATE: Admin room type save
-- Pages: POST /admin/rooms/save
-- ---------------------------------------------------------------------------
-- INSERT INTO room_types (hotel_id, room_name, max_adults, max_children, bed_type, price_per_night, total_rooms)
-- VALUES (?, ?, ?, ?, ?, ?, ?);
--
-- UPDATE room_types
-- SET hotel_id = ?, room_name = ?, max_adults = ?, max_children = ?, bed_type = ?, price_per_night = ?, total_rooms = ?
-- WHERE room_type_id = ?;

-- ---------------------------------------------------------------------------
-- SELECT + JOIN (optional): Latest payment for a booking — reserved for future UI
-- ---------------------------------------------------------------------------
-- SELECT payment_id, booking_id, amount, payment_method, payment_status, paid_at
-- FROM payments WHERE booking_id = ? ORDER BY payment_id DESC LIMIT 1;
