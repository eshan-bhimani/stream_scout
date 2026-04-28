package com.example.hotelbooking.controller;

import com.example.hotelbooking.model.Hotel;
import com.example.hotelbooking.model.RoomType;
import com.example.hotelbooking.service.AdminService;
import com.example.hotelbooking.service.HotelService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final HotelService hotelService;

    public AdminController(AdminService adminService, HotelService hotelService) {
        this.adminService = adminService;
        this.hotelService = hotelService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("summary", adminService.dashboardSummary());
        model.addAttribute("byHotel", adminService.analyticsByHotel());
        model.addAttribute("byMonth", adminService.bookingsByMonth());
        model.addAttribute("roomUsage", adminService.roomUsage());
        return "admin/dashboard";
    }

    @GetMapping("/bookings")
    public String bookings(@RequestParam(required = false) Integer hotelId,
                           @RequestParam(required = false) String status,
                           @RequestParam(required = false) Boolean canceledOnly,
                           Model model) {
        model.addAttribute("bookings", adminService.listBookings(hotelId, status, canceledOnly));
        model.addAttribute("hotels", hotelService.listHotels());
        model.addAttribute("hotelId", hotelId);
        model.addAttribute("status", status);
        model.addAttribute("canceledOnly", canceledOnly);
        return "admin/bookings";
    }

    @PostMapping("/bookings/status")
    public String updateStatus(@RequestParam int bookingId,
                               @RequestParam String bookingStatus,
                               @RequestParam(required = false) Boolean canceled,
                               RedirectAttributes redirectAttributes) {
        adminService.updateBookingStatus(bookingId, bookingStatus, Boolean.TRUE.equals(canceled));
        redirectAttributes.addFlashAttribute("message", "Booking updated.");
        return "redirect:/admin/bookings";
    }

    @PostMapping("/bookings/delete")
    public String deleteBooking(@RequestParam int bookingId, RedirectAttributes redirectAttributes) {
        if (adminService.deleteBookingAndPayments(bookingId)) {
            redirectAttributes.addFlashAttribute("message", "Booking and related payments removed.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Booking not found or could not be deleted.");
        }
        return "redirect:/admin/bookings";
    }

    @GetMapping("/hotels")
    public String hotels(Model model) {
        model.addAttribute("hotels", hotelService.listHotels());
        return "admin/hotels";
    }

    @GetMapping("/hotels/edit")
    public String hotelEdit(@RequestParam(required = false) Integer id, Model model) {
        Hotel h = new Hotel();
        if (id != null) {
            hotelService.getHotel(id).ifPresent(existing -> {
                h.setHotelId(existing.getHotelId());
                h.setHotelName(existing.getHotelName());
                h.setHotelType(existing.getHotelType());
                h.setCity(existing.getCity());
                h.setCountryCode(existing.getCountryCode());
                h.setDescription(existing.getDescription());
            });
        }
        model.addAttribute("hotel", h);
        return "admin/hotel-form";
    }

    @PostMapping("/hotels/save")
    public String saveHotel(@ModelAttribute Hotel hotel, RedirectAttributes redirectAttributes) {
        hotelService.saveHotel(hotel);
        redirectAttributes.addFlashAttribute("message", "Hotel saved.");
        return "redirect:/admin/hotels";
    }

    @GetMapping("/rooms")
    public String rooms(Model model) {
        model.addAttribute("roomTypes", hotelService.listAllRoomTypes());
        model.addAttribute("hotels", hotelService.listHotels());
        return "admin/rooms";
    }

    @GetMapping("/rooms/edit")
    public String roomEdit(@RequestParam(required = false) Integer id, Model model) {
        RoomType r = new RoomType();
        r.setMaxAdults(2);
        r.setMaxChildren(0);
        r.setTotalRooms(10);
        r.setPricePerNight(BigDecimal.valueOf(99));
        if (id != null) {
            hotelService.getRoomType(id).ifPresent(existing -> {
                r.setRoomTypeId(existing.getRoomTypeId());
                r.setHotelId(existing.getHotelId());
                r.setRoomName(existing.getRoomName());
                r.setMaxAdults(existing.getMaxAdults());
                r.setMaxChildren(existing.getMaxChildren());
                r.setBedType(existing.getBedType());
                r.setPricePerNight(existing.getPricePerNight());
                r.setTotalRooms(existing.getTotalRooms());
            });
        }
        model.addAttribute("roomType", r);
        model.addAttribute("hotels", hotelService.listHotels());
        return "admin/room-form";
    }

    @PostMapping("/rooms/save")
    public String saveRoom(@ModelAttribute RoomType roomType, RedirectAttributes redirectAttributes) {
        hotelService.saveRoomType(roomType);
        redirectAttributes.addFlashAttribute("message", "Room type saved.");
        return "redirect:/admin/rooms";
    }
}
