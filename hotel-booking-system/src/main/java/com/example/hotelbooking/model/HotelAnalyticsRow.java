package com.example.hotelbooking.model;

import java.math.BigDecimal;

public class HotelAnalyticsRow {
    private String hotelName;
    private Long totalBookings;
    private Long canceledBookings;
    private BigDecimal avgDailyRate;

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

    public Long getCanceledBookings() {
        return canceledBookings;
    }

    public void setCanceledBookings(Long canceledBookings) {
        this.canceledBookings = canceledBookings;
    }

    public BigDecimal getAvgDailyRate() {
        return avgDailyRate;
    }

    public void setAvgDailyRate(BigDecimal avgDailyRate) {
        this.avgDailyRate = avgDailyRate;
    }
}
