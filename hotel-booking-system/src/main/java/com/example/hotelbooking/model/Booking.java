package com.example.hotelbooking.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Booking {
    private Integer bookingId;
    private Integer userId;
    private Integer hotelId;
    private Integer roomTypeId;
    private LocalDateTime bookingDate;
    private LocalDate arrivalDate;
    private LocalDate checkoutDate;
    private Integer adults;
    private Integer children;
    private Integer babies;
    private String mealPlan;
    private String marketSegment;
    private String distributionChannel;
    private Boolean repeatedGuest;
    private Integer previousCancellations;
    private Integer previousBookingsNotCanceled;
    private String bookingStatus;
    private Boolean canceled;
    private BigDecimal adr;
    private Integer specialRequests;
    private Integer parkingSpaces;
    private Integer totalNights;

    public Integer getBookingId() {
        return bookingId;
    }

    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getHotelId() {
        return hotelId;
    }

    public void setHotelId(Integer hotelId) {
        this.hotelId = hotelId;
    }

    public Integer getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(Integer roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    public LocalDateTime getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDateTime bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalDate getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(LocalDate arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public LocalDate getCheckoutDate() {
        return checkoutDate;
    }

    public void setCheckoutDate(LocalDate checkoutDate) {
        this.checkoutDate = checkoutDate;
    }

    public Integer getAdults() {
        return adults;
    }

    public void setAdults(Integer adults) {
        this.adults = adults;
    }

    public Integer getChildren() {
        return children;
    }

    public void setChildren(Integer children) {
        this.children = children;
    }

    public Integer getBabies() {
        return babies;
    }

    public void setBabies(Integer babies) {
        this.babies = babies;
    }

    public String getMealPlan() {
        return mealPlan;
    }

    public void setMealPlan(String mealPlan) {
        this.mealPlan = mealPlan;
    }

    public String getMarketSegment() {
        return marketSegment;
    }

    public void setMarketSegment(String marketSegment) {
        this.marketSegment = marketSegment;
    }

    public String getDistributionChannel() {
        return distributionChannel;
    }

    public void setDistributionChannel(String distributionChannel) {
        this.distributionChannel = distributionChannel;
    }

    public Boolean getRepeatedGuest() {
        return repeatedGuest;
    }

    public void setRepeatedGuest(Boolean repeatedGuest) {
        this.repeatedGuest = repeatedGuest;
    }

    public Integer getPreviousCancellations() {
        return previousCancellations;
    }

    public void setPreviousCancellations(Integer previousCancellations) {
        this.previousCancellations = previousCancellations;
    }

    public Integer getPreviousBookingsNotCanceled() {
        return previousBookingsNotCanceled;
    }

    public void setPreviousBookingsNotCanceled(Integer previousBookingsNotCanceled) {
        this.previousBookingsNotCanceled = previousBookingsNotCanceled;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public Boolean getCanceled() {
        return canceled;
    }

    public void setCanceled(Boolean canceled) {
        this.canceled = canceled;
    }

    public BigDecimal getAdr() {
        return adr;
    }

    public void setAdr(BigDecimal adr) {
        this.adr = adr;
    }

    public Integer getSpecialRequests() {
        return specialRequests;
    }

    public void setSpecialRequests(Integer specialRequests) {
        this.specialRequests = specialRequests;
    }

    public Integer getParkingSpaces() {
        return parkingSpaces;
    }

    public void setParkingSpaces(Integer parkingSpaces) {
        this.parkingSpaces = parkingSpaces;
    }

    public Integer getTotalNights() {
        return totalNights;
    }

    public void setTotalNights(Integer totalNights) {
        this.totalNights = totalNights;
    }
}
