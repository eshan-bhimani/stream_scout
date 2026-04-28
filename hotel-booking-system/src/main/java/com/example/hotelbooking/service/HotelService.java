package com.example.hotelbooking.service;

import com.example.hotelbooking.model.Hotel;
import com.example.hotelbooking.model.RoomType;
import com.example.hotelbooking.repository.HotelRepository;
import com.example.hotelbooking.repository.RoomTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class HotelService {

    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;

    public HotelService(HotelRepository hotelRepository, RoomTypeRepository roomTypeRepository) {
        this.hotelRepository = hotelRepository;
        this.roomTypeRepository = roomTypeRepository;
    }

    public List<Hotel> listHotels() {
        return hotelRepository.findAllOrderedByName();
    }

    public Optional<Hotel> getHotel(int id) {
        return hotelRepository.findById(id);
    }

    public List<RoomType> roomTypesForHotel(int hotelId) {
        return roomTypeRepository.findByHotelId(hotelId);
    }

    @Transactional
    public void saveHotel(Hotel hotel) {
        Integer id = hotel.getHotelId();
        if (id == null || id == 0) {
            hotelRepository.insert(hotel);
        } else {
            hotelRepository.update(hotel);
        }
    }

    @Transactional
    public void saveRoomType(RoomType roomType) {
        Integer id = roomType.getRoomTypeId();
        if (id == null || id == 0) {
            roomTypeRepository.insert(roomType);
        } else {
            roomTypeRepository.update(roomType);
        }
    }

    public List<RoomType> listAllRoomTypes() {
        return roomTypeRepository.findAll();
    }

    public Optional<RoomType> getRoomType(int id) {
        return roomTypeRepository.findById(id);
    }
}
