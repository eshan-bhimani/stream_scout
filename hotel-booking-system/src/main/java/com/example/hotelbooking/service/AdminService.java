package com.example.hotelbooking.service;

import com.example.hotelbooking.model.*;
import com.example.hotelbooking.repository.BookingRepository;
import com.example.hotelbooking.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class AdminService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    public AdminService(BookingRepository bookingRepository, PaymentRepository paymentRepository) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
    }

    public List<AdminBookingRow> listBookings(Integer hotelId, String status, Boolean canceledOnly) {
        return bookingRepository.findAllForAdmin(hotelId, status, canceledOnly);
    }

    @Transactional
    public void updateBookingStatus(int bookingId, String status, boolean canceled) {
        bookingRepository.updateStatusByAdmin(bookingId, status, canceled);
    }

    /** Removes payment rows then the booking (admin maintenance / GDPR-style purge). */
    @Transactional
    public boolean deleteBookingAndPayments(int bookingId) {
        Objects.requireNonNull(bookingId);
        paymentRepository.deleteByBookingId(bookingId);
        return bookingRepository.deleteById(bookingId) > 0;
    }

    public Map<String, Object> dashboardSummary() {
        long total = bookingRepository.countAll();
        long canceled = bookingRepository.countCanceled();
        BigDecimal avgAdr = bookingRepository.avgAdr();
        if (avgAdr != null) {
            avgAdr = avgAdr.setScale(2, RoundingMode.HALF_UP);
        } else {
            avgAdr = BigDecimal.ZERO;
        }
        BigDecimal revenue = paymentRepository.sumCompletedAmount();
        double cancelRate = total > 0 ? (canceled * 100.0 / total) : 0.0;
        Map<String, Object> m = new HashMap<>();
        m.put("totalBookings", total);
        m.put("canceledBookings", canceled);
        m.put("cancellationRate", Math.round(cancelRate * 100.0) / 100.0);
        m.put("avgAdr", avgAdr);
        m.put("totalRevenue", revenue);
        return m;
    }

    public List<HotelAnalyticsRow> analyticsByHotel() {
        return bookingRepository.analyticsByHotel();
    }

    public List<MonthBookingCount> bookingsByMonth() {
        return bookingRepository.bookingCountsByMonth();
    }

    public List<RoomUsageRow> roomUsage() {
        return bookingRepository.roomUsageByType();
    }
}
