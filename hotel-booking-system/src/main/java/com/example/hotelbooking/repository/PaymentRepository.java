package com.example.hotelbooking.repository;

import com.example.hotelbooking.model.Payment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class PaymentRepository {

    private static final RowMapper<Payment> ROW_MAPPER = (rs, rowNum) -> {
        Payment p = new Payment();
        p.setPaymentId(rs.getInt("payment_id"));
        p.setBookingId(rs.getInt("booking_id"));
        p.setAmount(rs.getBigDecimal("amount"));
        p.setPaymentMethod(rs.getString("payment_method"));
        p.setPaymentStatus(rs.getString("payment_status"));
        Timestamp t = rs.getTimestamp("paid_at");
        if (t != null) {
            p.setPaidAt(t.toLocalDateTime());
        }
        return p;
    };

    private final JdbcTemplate jdbcTemplate;

    public PaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int insert(int bookingId, BigDecimal amount, String method, String status) {
        String sql = """
                INSERT INTO payments (booking_id, amount, payment_method, payment_status, paid_at)
                VALUES (?, ?, ?, ?, NOW())
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, bookingId);
            ps.setBigDecimal(2, amount);
            ps.setString(3, method);
            ps.setString(4, status);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null ? key.intValue() : 0;
    }

    public Optional<Payment> findByBookingId(int bookingId) {
        String sql = """
                SELECT payment_id, booking_id, amount, payment_method, payment_status, paid_at
                FROM payments
                WHERE booking_id = ?
                ORDER BY payment_id DESC
                LIMIT 1
                """;
        List<Payment> list = jdbcTemplate.query(sql, ROW_MAPPER, bookingId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public BigDecimal sumCompletedAmount() {
        BigDecimal v = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE payment_status = 'COMPLETED'",
                BigDecimal.class);
        return v != null ? v : BigDecimal.ZERO;
    }

    /** FK: remove payments before deleting the booking. Used by admin. */
    public int deleteByBookingId(int bookingId) {
        return jdbcTemplate.update("DELETE FROM payments WHERE booking_id = ?", bookingId);
    }
}
