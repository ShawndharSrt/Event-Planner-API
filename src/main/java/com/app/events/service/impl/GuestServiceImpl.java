package com.app.events.service.impl;

import com.app.events.model.Guest;
import com.app.events.repository.GuestRepository;
import com.app.events.service.GuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GuestServiceImpl implements GuestService {

    private final GuestRepository guestRepository;

    @Override
    public List<Guest> getAllGuests() {
        return guestRepository.findAll();
    }

    @Override
    public List<Guest> getGuestsByEventId(String eventId) {
        return guestRepository.findByEventId(eventId);
    }

    @Override
    public Optional<Guest> getGuestById(String id) {
        return guestRepository.findById(id);
    }

    @Override
    public Guest createGuest(Guest guest) {
        return guestRepository.save(guest);
    }

    @Override
    public Guest updateGuest(String id, Guest guest) {
        if (guestRepository.existsById(id)) {
            guest.setId(id);
            return guestRepository.save(guest);
        }
        throw new RuntimeException("Guest not found with id: " + id);
    }

    @Override
    public void deleteGuest(String id) {
        guestRepository.deleteById(id);
    }
}
