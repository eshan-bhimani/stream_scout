package com.example.hotelbooking.repository;

import com.example.hotelbooking.model.Hotel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class HotelRepository {

    private static final RowMapper<Hotel> ROW_MAPPER = (rs, rowNum) -> {
        Hotel h = new Hotel();
        h.setHotelId(rs.getInt("hotel_id"));
        h.setHotelName(rs.getString("hotel_name"));
        h.setHotelType(rs.getString("hotel_type"));
        h.setCity(rs.getString("city"));
        h.setCountryCode(rs.getString("country_code"));
        h.setDescription(rs.getString("description"));
        return h;
    };

    private final JdbcTemplate jdbcTemplate;

    public HotelRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Hotel> findAllOrderedByName() {
        String sql = """
                SELECT hotel_id, hotel_name, hotel_type, city, country_code, description
                FROM hotels
                ORDER BY hotel_name
                """;
        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    public Optional<Hotel> findById(int hotelId) {
        String sql = """
                SELECT hotel_id, hotel_name, hotel_type, city, country_code, description
                FROM hotels
                WHERE hotel_id = ?
                """;
        List<Hotel> list = jdbcTemplate.query(sql, ROW_MAPPER, hotelId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public int insert(Hotel hotel) {
        String sql = """
                INSERT INTO hotels (hotel_name, hotel_type, city, country_code, description)
                VALUES (?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, hotel.getHotelName());
            ps.setString(2, hotel.getHotelType());
            ps.setString(3, hotel.getCity());
            ps.setString(4, hotel.getCountryCode());
            ps.setString(5, hotel.getDescription());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null ? key.intValue() : 0;
    }

    public int update(Hotel hotel) {
        String sql = """
                UPDATE hotels
                SET hotel_name = ?, hotel_type = ?, city = ?, country_code = ?, description = ?
                WHERE hotel_id = ?
                """;
        return jdbcTemplate.update(sql,
                hotel.getHotelName(),
                hotel.getHotelType(),
                hotel.getCity(),
                hotel.getCountryCode(),
                hotel.getDescription(),
                hotel.getHotelId());
    }
}
