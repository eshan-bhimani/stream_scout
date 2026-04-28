package com.example.hotelbooking.repository;

import com.example.hotelbooking.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class BookingRepository {

    private final JdbcTemplate jdbcTemplate;

    public BookingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<Booking> BOOKING_ROW_MAPPER = (rs, rowNum) -> mapBooking(rs);

    private static Booking mapBooking(java.sql.ResultSet rs) throws java.sql.SQLException {
        Booking b = new Booking();
        b.setBookingId(rs.getInt("booking_id"));
        int uid = rs.getInt("user_id");
        if (!rs.wasNull()) {
            b.setUserId(uid);
        } else {
            b.setUserId(null);
        }
        b.setHotelId(rs.getInt("hotel_id"));
        b.setRoomTypeId(rs.getInt("room_type_id"));
        Timestamp bd = rs.getTimestamp("booking_date");
        if (bd != null) {
            b.setBookingDate(bd.toLocalDateTime());
        }
        Date ad = rs.getDate("arrival_date");
        if (ad != null) {
            b.setArrivalDate(ad.toLocalDate());
        }
        Date cd = rs.getDate("checkout_date");
        if (cd != null) {
            b.setCheckoutDate(cd.toLocalDate());
        }
        b.setAdults(rs.getInt("adults"));
        b.setChildren(rs.getInt("children"));
        b.setBabies(rs.getInt("babies"));
        b.setMealPlan(rs.getString("meal_plan"));
        b.setMarketSegment(rs.getString("market_segment"));
        b.setDistributionChannel(rs.getString("distribution_channel"));
        b.setRepeatedGuest(rs.getBoolean("is_repeated_guest"));
        b.setPreviousCancellations(rs.getInt("previous_cancellations"));
        b.setPreviousBookingsNotCanceled(rs.getInt("previous_bookings_not_canceled"));
        b.setBookingStatus(rs.getString("booking_status"));
        b.setCanceled(rs.getBoolean("canceled"));
        b.setAdr(rs.getBigDecimal("adr"));
        b.setSpecialRequests(rs.getInt("special_requests"));
        b.setParkingSpaces(rs.getInt("parking_spaces"));
        b.setTotalNights(rs.getInt("total_nights"));
        return b;
    }

    public Optional<Booking> findById(int bookingId) {
        String sql = """
                SELECT booking_id, user_id, hotel_id, room_type_id, booking_date, arrival_date, checkout_date,
                       adults, children, babies, meal_plan, market_segment, distribution_channel,
                       is_repeated_guest, previous_cancellations, previous_bookings_not_canceled,
                       booking_status, canceled, adr, special_requests, parking_spaces, total_nights
                FROM bookings
                WHERE booking_id = ?
                """;
        List<Booking> list = jdbcTemplate.query(sql, BOOKING_ROW_MAPPER, bookingId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<BookingSummary> findSummariesForUser(int userId) {
        String sql = """
                SELECT b.booking_id, h.hotel_name, r.room_name, b.arrival_date, b.checkout_date,
                       b.booking_status, b.canceled, b.adr
                FROM bookings b
                JOIN hotels h ON b.hotel_id = h.hotel_id
                JOIN room_types r ON b.room_type_id = r.room_type_id
                WHERE b.user_id = ?
                ORDER BY b.arrival_date DESC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            BookingSummary s = new BookingSummary();
            s.setBookingId(rs.getInt("booking_id"));
            s.setHotelName(rs.getString("hotel_name"));
            s.setRoomName(rs.getString("room_name"));
            s.setArrivalDate(rs.getDate("arrival_date").toLocalDate());
            s.setCheckoutDate(rs.getDate("checkout_date").toLocalDate());
            s.setBookingStatus(rs.getString("booking_status"));
            s.setCanceled(rs.getBoolean("canceled"));
            s.setAdr(rs.getBigDecimal("adr"));
            return s;
        }, userId);
    }

    public List<RoomAvailability> findAvailability(int hotelId, LocalDate arrival, LocalDate checkout) {
        String sql = """
                SELECT rt.room_type_id, rt.room_name, rt.total_rooms, rt.max_adults, rt.max_children,
                       rt.bed_type, rt.price_per_night,
                       COUNT(b.booking_id) AS booked_count
                FROM room_types rt
                LEFT JOIN bookings b
                  ON rt.room_type_id = b.room_type_id
                 AND b.canceled = 0
                 AND b.arrival_date < ?
                 AND b.checkout_date > ?
                WHERE rt.hotel_id = ?
                GROUP BY rt.room_type_id, rt.room_name, rt.total_rooms, rt.max_adults, rt.max_children,
                         rt.bed_type, rt.price_per_night
                ORDER BY rt.room_name
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            RoomAvailability ra = new RoomAvailability();
            ra.setRoomTypeId(rs.getInt("room_type_id"));
            ra.setRoomName(rs.getString("room_name"));
            ra.setTotalRooms(rs.getInt("total_rooms"));
            ra.setMaxAdults(rs.getInt("max_adults"));
            ra.setMaxChildren(rs.getInt("max_children"));
            ra.setBedType(rs.getString("bed_type"));
            ra.setPricePerNight(rs.getBigDecimal("price_per_night"));
            ra.setBookedCount(rs.getLong("booked_count"));
            return ra;
        }, Date.valueOf(checkout), Date.valueOf(arrival), hotelId);
    }

    public int insert(Booking b) {
        String sql = """
                INSERT INTO bookings (
                    user_id, hotel_id, room_type_id, booking_date, arrival_date, checkout_date,
                    adults, children, babies, meal_plan, market_segment, distribution_channel,
                    is_repeated_guest, previous_cancellations, previous_bookings_not_canceled,
                    booking_status, canceled, adr, special_requests, parking_spaces, total_nights
                ) VALUES (?, ?, ?, NOW(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            if (b.getUserId() != null) {
                ps.setInt(1, b.getUserId());
            } else {
                ps.setObject(1, null);
            }
            ps.setInt(2, b.getHotelId());
            ps.setInt(3, b.getRoomTypeId());
            ps.setDate(4, Date.valueOf(b.getArrivalDate()));
            ps.setDate(5, Date.valueOf(b.getCheckoutDate()));
            ps.setInt(6, b.getAdults());
            ps.setInt(7, b.getChildren() != null ? b.getChildren() : 0);
            ps.setInt(8, b.getBabies() != null ? b.getBabies() : 0);
            ps.setString(9, b.getMealPlan());
            ps.setString(10, b.getMarketSegment());
            ps.setString(11, b.getDistributionChannel());
            ps.setBoolean(12, Boolean.TRUE.equals(b.getRepeatedGuest()));
            ps.setInt(13, b.getPreviousCancellations() != null ? b.getPreviousCancellations() : 0);
            ps.setInt(14, b.getPreviousBookingsNotCanceled() != null ? b.getPreviousBookingsNotCanceled() : 0);
            ps.setString(15, b.getBookingStatus());
            ps.setBoolean(16, Boolean.TRUE.equals(b.getCanceled()));
            ps.setBigDecimal(17, b.getAdr());
            ps.setInt(18, b.getSpecialRequests() != null ? b.getSpecialRequests() : 0);
            ps.setInt(19, b.getParkingSpaces() != null ? b.getParkingSpaces() : 0);
            ps.setInt(20, b.getTotalNights());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null ? key.intValue() : 0;
    }

    public int cancelForUser(int bookingId, int userId) {
        String sql = """
                UPDATE bookings
                SET canceled = 1, booking_status = 'CANCELED'
                WHERE booking_id = ? AND user_id = ?
                """;
        return jdbcTemplate.update(sql, bookingId, userId);
    }

    public int updateBookingDetails(int bookingId, int userId, LocalDate arrival, LocalDate checkout,
                                    int adults, int children, int babies, String mealPlan) {
        String sql = """
                UPDATE bookings
                SET arrival_date = ?, checkout_date = ?, adults = ?, children = ?, babies = ?, meal_plan = ?,
                    total_nights = ?
                WHERE booking_id = ? AND user_id = ? AND canceled = 0
                """;
        int nights = (int) java.time.temporal.ChronoUnit.DAYS.between(arrival, checkout);
        if (nights < 1) {
            nights = 1;
        }
        return jdbcTemplate.update(sql,
                Date.valueOf(arrival),
                Date.valueOf(checkout),
                adults,
                children,
                babies,
                mealPlan,
                nights,
                bookingId,
                userId);
    }

    public int updateStatusByAdmin(int bookingId, String status, boolean canceled) {
        String sql = "UPDATE bookings SET booking_status = ?, canceled = ? WHERE booking_id = ?";
        return jdbcTemplate.update(sql, status, canceled, bookingId);
    }

    /** Hard delete (admin). Payments must be deleted first. */
    public int deleteById(int bookingId) {
        return jdbcTemplate.update("DELETE FROM bookings WHERE booking_id = ?", bookingId);
    }

    public List<AdminBookingRow> findAllForAdmin(Integer hotelIdFilter, String statusFilter, Boolean canceledOnly) {
        StringBuilder sql = new StringBuilder("""
                SELECT b.booking_id, b.user_id, u.email AS user_email, h.hotel_name, r.room_name,
                       b.booking_date, b.arrival_date, b.checkout_date, b.booking_status, b.canceled, b.adr
                FROM bookings b
                JOIN hotels h ON b.hotel_id = h.hotel_id
                JOIN room_types r ON b.room_type_id = r.room_type_id
                LEFT JOIN users u ON b.user_id = u.user_id
                WHERE 1=1
                """);
        List<Object> args = new ArrayList<>();
        if (hotelIdFilter != null) {
            sql.append(" AND b.hotel_id = ?");
            args.add(hotelIdFilter);
        }
        if (statusFilter != null && !statusFilter.isBlank()) {
            sql.append(" AND b.booking_status = ?");
            args.add(statusFilter);
        }
        if (Boolean.TRUE.equals(canceledOnly)) {
            sql.append(" AND b.canceled = 1");
        }
        sql.append(" ORDER BY b.arrival_date DESC LIMIT 500");

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            AdminBookingRow row = new AdminBookingRow();
            row.setBookingId(rs.getInt("booking_id"));
            int uid = rs.getInt("user_id");
            if (!rs.wasNull()) {
                row.setUserId(uid);
            }
            row.setUserEmail(rs.getString("user_email"));
            row.setHotelName(rs.getString("hotel_name"));
            row.setRoomName(rs.getString("room_name"));
            Timestamp bd = rs.getTimestamp("booking_date");
            if (bd != null) {
                row.setBookingDate(bd.toLocalDateTime());
            }
            row.setArrivalDate(rs.getDate("arrival_date").toLocalDate());
            row.setCheckoutDate(rs.getDate("checkout_date").toLocalDate());
            row.setBookingStatus(rs.getString("booking_status"));
            row.setCanceled(rs.getBoolean("canceled"));
            row.setAdr(rs.getBigDecimal("adr"));
            return row;
        }, args.toArray());
    }

    public long countAll() {
        Long n = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM bookings", Long.class);
        return n != null ? n : 0;
    }

    public long countCanceled() {
        Long n = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM bookings WHERE canceled = 1", Long.class);
        return n != null ? n : 0;
    }

    public java.math.BigDecimal avgAdr() {
        return jdbcTemplate.queryForObject("SELECT AVG(adr) FROM bookings WHERE adr IS NOT NULL AND adr > 0", java.math.BigDecimal.class);
    }

    public List<HotelAnalyticsRow> analyticsByHotel() {
        String sql = """
                SELECT h.hotel_name,
                       COUNT(*) AS total_bookings,
                       SUM(CASE WHEN b.canceled = 1 THEN 1 ELSE 0 END) AS canceled_bookings,
                       ROUND(AVG(b.adr), 2) AS avg_daily_rate
                FROM bookings b
                JOIN hotels h ON b.hotel_id = h.hotel_id
                GROUP BY h.hotel_id, h.hotel_name
                ORDER BY total_bookings DESC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            HotelAnalyticsRow row = new HotelAnalyticsRow();
            row.setHotelName(rs.getString("hotel_name"));
            row.setTotalBookings(rs.getLong("total_bookings"));
            row.setCanceledBookings(rs.getLong("canceled_bookings"));
            row.setAvgDailyRate(rs.getBigDecimal("avg_daily_rate"));
            return row;
        });
    }

    public List<MonthBookingCount> bookingCountsByMonth() {
        String sql = """
                SELECT YEAR(arrival_date) AS yr,
                       MONTH(arrival_date) AS mn,
                       COUNT(*) AS total_bookings
                FROM bookings
                GROUP BY YEAR(arrival_date), MONTH(arrival_date)
                ORDER BY yr, mn
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            MonthBookingCount m = new MonthBookingCount();
            m.setYear(rs.getInt("yr"));
            m.setMonth(rs.getInt("mn"));
            m.setTotalBookings(rs.getLong("total_bookings"));
            return m;
        });
    }

    public List<RoomUsageRow> roomUsageByType() {
        String sql = """
                SELECT r.room_name,
                       h.hotel_name,
                       COUNT(*) AS total_bookings,
                       ROUND(AVG(b.total_nights), 2) AS avg_stay_length
                FROM bookings b
                JOIN room_types r ON b.room_type_id = r.room_type_id
                JOIN hotels h ON b.hotel_id = h.hotel_id
                GROUP BY r.room_type_id, r.room_name, h.hotel_name
                ORDER BY total_bookings DESC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            RoomUsageRow row = new RoomUsageRow();
            row.setRoomName(rs.getString("room_name"));
            row.setHotelName(rs.getString("hotel_name"));
            row.setTotalBookings(rs.getLong("total_bookings"));
            row.setAvgStayLength(rs.getBigDecimal("avg_stay_length"));
            return row;
        });
    }
}
