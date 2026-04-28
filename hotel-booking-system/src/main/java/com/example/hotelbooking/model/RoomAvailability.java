package com.example.hotelbooking.model;

import java.math.BigDecimal;

public class RoomAvailability {
    private Integer roomTypeId;
    private String roomName;
    private Integer totalRooms;
    private Long bookedCount;
    private Integer maxAdults;
    private Integer maxChildren;
    private String bedType;
    private BigDecimal pricePerNight;

    public Integer getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(Integer roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public Integer getTotalRooms() {
        return totalRooms;
    }

    public void setTotalRooms(Integer totalRooms) {
        this.totalRooms = totalRooms;
    }

    public Long getBookedCount() {
        return bookedCount;
    }

    public void setBookedCount(Long bookedCount) {
        this.bookedCount = bookedCount;
    }

    public Integer getMaxAdults() {
        return maxAdults;
    }

    public void setMaxAdults(Integer maxAdults) {
        this.maxAdults = maxAdults;
    }

    public Integer getMaxChildren() {
        return maxChildren;
    }

    public void setMaxChildren(Integer maxChildren) {
        this.maxChildren = maxChildren;
    }

    public String getBedType() {
        return bedType;
    }

    public void setBedType(String bedType) {
        this.bedType = bedType;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public boolean isAvailable() {
        return totalRooms != null && bookedCount != null && totalRooms > bookedCount;
    }

    public long getAvailableCount() {
        if (totalRooms == null || bookedCount == null) {
            return 0;
        }
        return Math.max(0, totalRooms - bookedCount);
    }
}
