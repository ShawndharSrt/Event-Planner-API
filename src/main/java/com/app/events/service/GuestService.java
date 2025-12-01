package com.app.events.service;

import com.app.events.model.Guest;
import java.util.List;
import java.util.Optional;

public interface GuestService {
    List<Guest> getAllGuests();

    List<Guest> getGuestsByEventId(String eventId);

    Optional<Guest> getGuestById(String id);

    Guest createGuest(Guest guest);

    Guest updateGuest(String id, Guest guest);

    void deleteGuest(String id);
}
