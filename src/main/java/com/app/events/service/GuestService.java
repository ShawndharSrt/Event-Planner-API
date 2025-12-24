package com.app.events.service;

import com.app.events.model.Guest;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface GuestService {
    Page<Guest> getAllGuests(int page, int size);

    long countGuests();

    Optional<Guest> getGuestById(String id);

    Guest createGuest(Guest guest);

    Guest updateGuest(String id, Guest guest);

    void deleteGuest(String id);
}
