package com.app.events.web.controller;

import com.app.events.dto.ApiResponse;
import com.app.events.model.Guest;
import com.app.events.service.GuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guests")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GuestController {

    private final GuestService guestService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Guest>>> getAllGuests() {
        List<Guest> guests = guestService.getAllGuests();
        return ResponseEntity.ok(ApiResponse.success("Guests fetched successfully", guests));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Guest>> getGuestById(@PathVariable String id) {
        return guestService.getGuestById(id)
                .map(guest -> ResponseEntity.ok(ApiResponse.success("Guest fetched successfully", guest)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Guest>> createGuest(@RequestBody Guest guest) {
        return ResponseEntity.ok(ApiResponse.success("Guest created successfully", guestService.createGuest(guest)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Guest>> updateGuest(@PathVariable String id, @RequestBody Guest guest) {
        return ResponseEntity
                .ok(ApiResponse.success("Guest updated successfully", guestService.updateGuest(id, guest)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGuest(@PathVariable String id) {
        guestService.deleteGuest(id);
        return ResponseEntity.ok(ApiResponse.success("Guest deleted successfully", null));
    }
}
