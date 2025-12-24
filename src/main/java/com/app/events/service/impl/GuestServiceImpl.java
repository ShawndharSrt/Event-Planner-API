package com.app.events.service.impl;

import com.app.events.dto.EventGuestResponse;
import com.app.events.model.Guest;
import com.app.events.repository.GuestRepository;
import com.app.events.service.GuestService;
import com.app.events.util.RestPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuestServiceImpl implements GuestService {

    private final GuestRepository guestRepository;

    @Override
    @Cacheable(value = "eventGuests", key = "'guests::' + #page + ':' + #size")
    public Page<Guest> getAllGuests(int page, int size) {
        log.info("CACHE MISS: Fetching guests from database for page {} and size {}", page, size);
        return new RestPage<>(guestRepository.findAll(PageRequest.of(page, size)));
    }

    @Override
    public long countGuests() {
        return guestRepository.count();
    }

    @Override
    public Optional<Guest> getGuestById(String id) {
        return guestRepository.findById(id);
    }

    @Override
    @CacheEvict(value = "eventGuests", allEntries = true)
    public Guest createGuest(Guest guest) {
        log.info("CACHE EVICT: Creating guest, clearing all guest caches.");
        LocalDateTime now = LocalDateTime.now();
        guest.setCreatedAt(now);
        guest.setUpdatedAt(now);
        return guestRepository.save(guest);
    }

    @Override
    @CacheEvict(value = "eventGuests", allEntries = true)
    public Guest updateGuest(String id, Guest guest) {
        log.info("CACHE EVICT: Updating guest {}, clearing all guest caches.", id);
        if (guestRepository.existsById(id)) {
            guest.setId(id);
            guest.setUpdatedAt(LocalDateTime.now());
            return guestRepository.save(guest);
        }
        throw new RuntimeException("Guest not found with id: " + id);
    }

    @Override
    @CacheEvict(value = "eventGuests", allEntries = true)
    public void deleteGuest(String id) {
        log.info("CACHE EVICT: Deleting guest {}, clearing all guest caches.", id);
        guestRepository.deleteById(id);
    }
}
