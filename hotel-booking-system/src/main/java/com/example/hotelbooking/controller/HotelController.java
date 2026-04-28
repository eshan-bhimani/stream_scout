package com.example.hotelbooking.controller;

import com.example.hotelbooking.service.HotelService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/hotels")
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("hotels", hotelService.listHotels());
        return "hotels/hotels";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable("id") int id, Model model) {
        return hotelService.getHotel(id)
                .map(h -> {
                    model.addAttribute("hotel", h);
                    model.addAttribute("roomTypes", hotelService.roomTypesForHotel(id));
                    return "hotels/hotel-details";
                })
                .orElse("redirect:/hotels");
    }
}
