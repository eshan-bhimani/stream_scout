package com.example.hotelbooking.repository;

import com.example.hotelbooking.model.RoomType;
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
public class RoomTypeRepository {

    private static final RowMapper<RoomType> ROW_MAPPER = (rs, rowNum) -> {
        RoomType r = new RoomType();
        r.setRoomTypeId(rs.getInt("room_type_id"));
        r.setHotelId(rs.getInt("hotel_id"));
        r.setRoomName(rs.getString("room_name"));
        r.setMaxAdults(rs.getInt("max_adults"));
        r.setMaxChildren(rs.getInt("max_children"));
        r.setBedType(rs.getString("bed_type"));
        r.setPricePerNight(rs.getBigDecimal("price_per_night"));
        r.setTotalRooms(rs.getInt("total_rooms"));
        return r;
    };

    private final JdbcTemplate jdbcTemplate;

    public RoomTypeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<RoomType> findByHotelId(int hotelId) {
        String sql = """
                SELECT room_type_id, hotel_id, room_name, max_adults, max_children, bed_type, price_per_night, total_rooms
                FROM room_types
                WHERE hotel_id = ?
                ORDER BY room_name
                """;
        return jdbcTemplate.query(sql, ROW_MAPPER, hotelId);
    }

    public List<RoomType> findAll() {
        String sql = """
                SELECT room_type_id, hotel_id, room_name, max_adults, max_children, bed_type, price_per_night, total_rooms
                FROM room_types
                ORDER BY hotel_id, room_name
                """;
        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    public Optional<RoomType> findById(int roomTypeId) {
        String sql = """
                SELECT room_type_id, hotel_id, room_name, max_adults, max_children, bed_type, price_per_night, total_rooms
                FROM room_types
                WHERE room_type_id = ?
                """;
        List<RoomType> list = jdbcTemplate.query(sql, ROW_MAPPER, roomTypeId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public int insert(RoomType r) {
        String sql = """
                INSERT INTO room_types (hotel_id, room_name, max_adults, max_children, bed_type, price_per_night, total_rooms)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, r.getHotelId());
            ps.setString(2, r.getRoomName());
            ps.setInt(3, r.getMaxAdults());
            ps.setInt(4, r.getMaxChildren());
            ps.setString(5, r.getBedType());
            ps.setBigDecimal(6, r.getPricePerNight());
            ps.setInt(7, r.getTotalRooms());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null ? key.intValue() : 0;
    }

    public int update(RoomType r) {
        String sql = """
                UPDATE room_types
                SET hotel_id = ?, room_name = ?, max_adults = ?, max_children = ?, bed_type = ?, price_per_night = ?, total_rooms = ?
                WHERE room_type_id = ?
                """;
        return jdbcTemplate.update(sql,
                r.getHotelId(),
                r.getRoomName(),
                r.getMaxAdults(),
                r.getMaxChildren(),
                r.getBedType(),
                r.getPricePerNight(),
                r.getTotalRooms(),
                r.getRoomTypeId());
    }
}
