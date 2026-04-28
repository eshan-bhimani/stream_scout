package com.example.hotelbooking.service;

import com.example.hotelbooking.model.Booking;
import com.example.hotelbooking.model.BookingSummary;
import com.example.hotelbooking.model.RoomAvailability;
import com.example.hotelbooking.model.RoomType;
import com.example.hotelbooking.repository.BookingRepository;
import com.example.hotelbooking.repository.PaymentRepository;
import com.example.hotelbooking.repository.RoomTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final PaymentRepository paymentRepository;

    public BookingService(BookingRepository bookingRepository,
                          RoomTypeRepository roomTypeRepository,
                          PaymentRepository paymentRepository) {
        this.bookingRepository = bookingRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.paymentRepository = paymentRepository;
    }

    public List<BookingSummary> myBookings(int userId) {
        return bookingRepository.findSummariesForUser(userId);
    }

    public Optional<Booking> getBooking(int bookingId) {
        return bookingRepository.findById(bookingId);
    }

    public List<RoomAvailability> searchAvailability(int hotelId, LocalDate arrival, LocalDate checkout) {
        if (!checkout.isAfter(arrival)) {
            throw new IllegalArgumentException("Checkout must be after arrival");
        }
        return bookingRepository.findAvailability(hotelId, arrival, checkout);
    }

    @Transactional
    public int createBooking(int userId, int hotelId, int roomTypeId, LocalDate arrival, LocalDate checkout,
                             int adults, int children, int babies, String mealPlan) {
        if (!checkout.isAfter(arrival)) {
            throw new IllegalArgumentException("Checkout must be after arrival");
        }
        List<RoomAvailability> avail = bookingRepository.findAvailability(hotelId, arrival, checkout);
        RoomAvailability match = avail.stream()
                .filter(a -> a.getRoomTypeId() == roomTypeId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Room type not found for hotel"));
        if (!match.isAvailable()) {
            throw new IllegalStateException("No rooms available for these dates");
        }
        RoomType rt = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid room type"));
        if (rt.getHotelId() != hotelId) {
            throw new IllegalArgumentException("Room does not belong to hotel");
        }
        int nights = (int) java.time.temporal.ChronoUnit.DAYS.between(arrival, checkout);
        if (nights < 1) {
            nights = 1;
        }
        Booking b = new Booking();
        b.setUserId(userId);
        b.setHotelId(hotelId);
        b.setRoomTypeId(roomTypeId);
        b.setArrivalDate(arrival);
        b.setCheckoutDate(checkout);
        b.setAdults(adults);
        b.setChildren(children);
        b.setBabies(babies);
        b.setMealPlan(mealPlan != null ? mealPlan : "BB");
        b.setMarketSegment("Direct");
        b.setDistributionChannel("Direct");
        b.setRepeatedGuest(false);
        b.setPreviousCancellations(0);
        b.setPreviousBookingsNotCanceled(0);
        b.setBookingStatus("BOOKED");
        b.setCanceled(false);
        b.setAdr(rt.getPricePerNight());
        b.setSpecialRequests(0);
        b.setParkingSpaces(0);
        b.setTotalNights(nights);
        int newId = bookingRepository.insert(b);
        BigDecimal amount = rt.getPricePerNight().multiply(BigDecimal.valueOf(nights)).setScale(2, RoundingMode.HALF_UP);
        paymentRepository.insert(newId, amount, "CARD", "COMPLETED");
        return newId;
    }

    @Transactional
    public boolean cancelBooking(int bookingId, int userId) {
        return bookingRepository.cancelForUser(bookingId, userId) > 0;
    }

    @Transactional
    public boolean updateBooking(int bookingId, int userId, LocalDate arrival, LocalDate checkout,
                                 int adults, int children, int babies, String mealPlan) {
        return bookingRepository.updateBookingDetails(bookingId, userId, arrival, checkout, adults, children, babies, mealPlan) > 0;
    }
}
