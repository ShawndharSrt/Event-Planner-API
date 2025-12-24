package com.app.events.service.impl;

import com.app.events.model.Guest;
import com.app.events.repository.GuestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuestServiceTest {

    @Mock
    private GuestRepository guestRepository;

    @InjectMocks
    private GuestServiceImpl guestService;

    private Guest guest;

    @BeforeEach
    void setUp() {
        guest = new Guest();
        guest.setId("gst-1");
        guest.setFirstName("John");
        guest.setLastName("Doe");
        guest.setEmail("john@example.com");
    }

    @Test
    void createGuest_shouldSaveGuest() {
        when(guestRepository.save(any(Guest.class))).thenReturn(guest);

        Guest created = guestService.createGuest(guest);

        assertNotNull(created);
        assertEquals("gst-1", created.getId());
        verify(guestRepository).save(guest);
    }

    @Test
    void getGuestById_shouldReturnGuest() {
        when(guestRepository.findById("gst-1")).thenReturn(Optional.of(guest));

        Optional<Guest> found = guestService.getGuestById("gst-1");

        assertTrue(found.isPresent());
        assertEquals("gst-1", found.get().getId());
    }

    @Test
    void getAllGuests_shouldReturnList() {
        when(guestRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(guest)));

        assertEquals(1, guestService.getAllGuests(0, 10).getContent().size());
    }

    @Test
    void updateGuest_shouldUpdateWhenFound() {
        when(guestRepository.existsById("gst-1")).thenReturn(true);
        when(guestRepository.save(any(Guest.class))).thenReturn(guest);

        Guest updated = guestService.updateGuest("gst-1", guest);

        assertNotNull(updated);
        verify(guestRepository).save(guest);
    }

    @Test
    void updateGuest_shouldThrowWhenNotFound() {
        when(guestRepository.existsById("gst-999")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> guestService.updateGuest("gst-999", guest));
    }

    @Test
    void deleteGuest_shouldDeleteById() {
        doNothing().when(guestRepository).deleteById("gst-1");

        guestService.deleteGuest("gst-1");

        verify(guestRepository).deleteById("gst-1");
    }
}
