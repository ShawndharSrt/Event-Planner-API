package com.app.events.service.impl;

import com.app.events.dto.EventGuestResponse;
import com.app.events.model.Guest;
import com.app.events.model.GuestEvent;
import com.app.events.repository.GuestEventRepository;
import com.app.events.repository.GuestRepository;
import com.app.events.service.GuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GuestServiceImpl implements GuestService {

    private final GuestRepository guestRepository;
    private final GuestEventRepository guestEventRepository;

    @Override
    public List<Guest> getAllGuests() {
        return guestRepository.findAll();
    }

    @Override
    public List<EventGuestResponse> getEventGuestsByEventId(String eventId) {
        // Get all guest-event relationships for this event
        List<GuestEvent> guestEvents = guestEventRepository.findByEventId(eventId);

        // For each relationship, fetch the guest details and combine
        return guestEvents.stream()
                .map(guestEvent -> {
                    Optional<Guest> guestOpt = guestRepository.findByGuestId(guestEvent.getGuestId());
                    if (guestOpt.isPresent()) {
                        Guest guest = guestOpt.get();
                        return new EventGuestResponse(
                                guest.getGuestId(),
                                guest.getFirstName(),
                                guest.getLastName(),
                                guest.getEmail(),
                                guest.getPhone(),
                                guestEvent.getGroup(),
                                guestEvent.getStatus(),
                                guestEvent.getDietary(),
                                guestEvent.getNotes());
                    }
                    return null;
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());
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
