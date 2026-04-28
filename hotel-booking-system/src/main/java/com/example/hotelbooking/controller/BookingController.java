package com.example.hotelbooking.controller;

import com.example.hotelbooking.model.Booking;
import com.example.hotelbooking.service.BookingService;
import com.example.hotelbooking.service.HotelService;
import com.example.hotelbooking.util.SecurityUtil;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final HotelService hotelService;
    private final SecurityUtil securityUtil;

    public BookingController(BookingService bookingService, HotelService hotelService, SecurityUtil securityUtil) {
        this.bookingService = bookingService;
        this.hotelService = hotelService;
        this.securityUtil = securityUtil;
    }

    @GetMapping("/search")
    public String search(@RequestParam int hotelId,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate arrival,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkout,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (!checkout.isAfter(arrival)) {
            redirectAttributes.addFlashAttribute("error", "Checkout must be after arrival.");
            return "redirect:/";
        }
        model.addAttribute("hotel", hotelService.getHotel(hotelId).orElse(null));
        model.addAttribute("hotelId", hotelId);
        model.addAttribute("arrival", arrival);
        model.addAttribute("checkout", checkout);
        model.addAttribute("results", bookingService.searchAvailability(hotelId, arrival, checkout));
        return "bookings/search-results";
    }

    @PostMapping
    public String create(@RequestParam int hotelId,
                         @RequestParam int roomTypeId,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate arrival,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkout,
                         @RequestParam(defaultValue = "2") int adults,
                         @RequestParam(defaultValue = "0") int children,
                         @RequestParam(defaultValue = "0") int babies,
                         @RequestParam(defaultValue = "BB") String mealPlan,
                         RedirectAttributes redirectAttributes) {
        int userId = securityUtil.requireUserId();
        try {
            int id = bookingService.createBooking(userId, hotelId, roomTypeId, arrival, checkout,
                    adults, children, babies, mealPlan);
            redirectAttributes.addFlashAttribute("message", "Booking confirmed.");
            return "redirect:/bookings/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/bookings/search?hotelId=" + hotelId + "&arrival=" + arrival + "&checkout=" + checkout;
        }
    }

    @GetMapping("/my")
    public String myBookings(Model model) {
        int userId = securityUtil.requireUserId();
        model.addAttribute("bookings", bookingService.myBookings(userId));
        return "bookings/my-bookings";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable int id, Model model, RedirectAttributes redirectAttributes) {
        Booking b = bookingService.getBooking(id).orElse(null);
        if (b == null) {
            return "redirect:/bookings/my";
        }
        boolean admin = securityUtil.isAdmin();
        int userId = securityUtil.currentUser().map(com.example.hotelbooking.model.User::getUserId).orElse(-1);
        if (!admin && (b.getUserId() == null || !b.getUserId().equals(userId))) {
            redirectAttributes.addFlashAttribute("error", "You cannot view this booking.");
            return "redirect:/bookings/my";
        }
        model.addAttribute("booking", b);
        hotelService.getHotel(b.getHotelId()).ifPresent(h -> model.addAttribute("hotelName", h.getHotelName()));
        hotelService.getRoomType(b.getRoomTypeId()).ifPresent(r -> model.addAttribute("roomName", r.getRoomName()));
        return "bookings/booking-details";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable int id, RedirectAttributes redirectAttributes) {
        int userId = securityUtil.requireUserId();
        if (bookingService.cancelBooking(id, userId)) {
            redirectAttributes.addFlashAttribute("message", "Booking canceled.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Could not cancel booking.");
        }
        return "redirect:/bookings/my";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable int id, Model model, RedirectAttributes redirectAttributes) {
        Booking b = bookingService.getBooking(id).orElse(null);
        if (b == null || Boolean.TRUE.equals(b.getCanceled())) {
            return "redirect:/bookings/my";
        }
        int userId = securityUtil.requireUserId();
        if (b.getUserId() == null || !b.getUserId().equals(userId)) {
            redirectAttributes.addFlashAttribute("error", "You cannot edit this booking.");
            return "redirect:/bookings/my";
        }
        model.addAttribute("booking", b);
        return "bookings/booking-edit";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable int id,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate arrival,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkout,
                         @RequestParam int adults,
                         @RequestParam int children,
                         @RequestParam int babies,
                         @RequestParam String mealPlan,
                         RedirectAttributes redirectAttributes) {
        int userId = securityUtil.requireUserId();
        if (bookingService.updateBooking(id, userId, arrival, checkout, adults, children, babies, mealPlan)) {
            redirectAttributes.addFlashAttribute("message", "Booking updated.");
            return "redirect:/bookings/" + id;
        }
        redirectAttributes.addFlashAttribute("error", "Update failed.");
        return "redirect:/bookings/" + id + "/edit";
    }
}
