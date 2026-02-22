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

import com.app.events.repository.UserRepository;
import com.app.events.util.AppUtils;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuestServiceImpl implements GuestService {

    private final GuestRepository guestRepository;
    private final UserRepository userRepository;

    @Override
    @Cacheable(value = "eventGuests", key = "'guests::' + #page + ':' + #size")
    public Page<Guest> getAllGuests(int page, int size) {
        log.info("CACHE MISS: Fetching guests from database for page {} and size {}", page, size);
        String currentUserId = AppUtils.getCurrentUserId();
        if (isAdmin(currentUserId)) {
            return new RestPage<>(guestRepository.findAll(PageRequest.of(page, size)));
        }
        return new RestPage<>(guestRepository.findByCreatedBy(currentUserId, PageRequest.of(page, size)));
    }

    @Override
    public long countGuests() {
        String currentUserId = AppUtils.getCurrentUserId();
        if (isAdmin(currentUserId)) {
            return guestRepository.count();
        }
        return guestRepository.countByCreatedBy(currentUserId);
    }

    @Override
    public Optional<Guest> getGuestById(String id) {
        String currentUserId = AppUtils.getCurrentUserId();
        return guestRepository.findById(id)
                .filter(guest -> isAdmin(currentUserId) || currentUserId.equals(guest.getCreatedBy()));
    }

    @Override
    @CacheEvict(value = "eventGuests", allEntries = true)
    public Guest createGuest(Guest guest) {
        log.info("CACHE EVICT: Creating guest, clearing all guest caches.");
        LocalDateTime now = LocalDateTime.now();
        guest.setCreatedAt(now);
        guest.setUpdatedAt(now);
        guest.setCreatedBy(AppUtils.getCurrentUserId());
        return guestRepository.save(guest);
    }

    @Override
    @CacheEvict(value = "eventGuests", allEntries = true)
    public Guest updateGuest(String id, Guest guest) {
        log.info("CACHE EVICT: Updating guest {}, clearing all guest caches.", id);
        return guestRepository.findById(id).map(existing -> {
            String currentUserId = AppUtils.getCurrentUserId();
            if (!isAdmin(currentUserId) && !currentUserId.equals(existing.getCreatedBy())) {
                throw new RuntimeException("Unauthorized to update this guest");
            }
            guest.setId(id);
            guest.setUpdatedAt(LocalDateTime.now());
            guest.setCreatedAt(existing.getCreatedAt());
            guest.setCreatedBy(existing.getCreatedBy());
            return guestRepository.save(guest);
        }).orElseThrow(() -> new RuntimeException("Guest not found with id: " + id));
    }

    @Override
    @CacheEvict(value = "eventGuests", allEntries = true)
    public void deleteGuest(String id) {
        log.info("CACHE EVICT: Deleting guest {}, clearing all guest caches.", id);
        String currentUserId = AppUtils.getCurrentUserId();
        Guest existing = guestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Guest not found with id: " + id));
        if (!isAdmin(currentUserId) && !currentUserId.equals(existing.getCreatedBy())) {
            throw new RuntimeException("Unauthorized to delete this guest");
        }
        guestRepository.deleteById(id);
    }

    private boolean isAdmin(String userId) {
        if (userId == null)
            return false;
        return userRepository.findByUserId(userId)
                .map(com.app.events.model.User::getRole)
                .map(roles -> roles.stream().anyMatch(role -> "ADMIN".equalsIgnoreCase(role)))
                .orElse(false);
    }
}
