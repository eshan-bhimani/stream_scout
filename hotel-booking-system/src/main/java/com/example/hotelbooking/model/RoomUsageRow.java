package com.example.hotelbooking.model;

import java.math.BigDecimal;

public class RoomUsageRow {
    private String roomName;
    private String hotelName;
    private Long totalBookings;
    private BigDecimal avgStayLength;

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public Long getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(Long totalBookings) {
        this.totalBookings = totalBookings;
    }

    public BigDecimal getAvgStayLength() {
        return avgStayLength;
    }

    public void setAvgStayLength(BigDecimal avgStayLength) {
        this.avgStayLength = avgStayLength;
    }
}
