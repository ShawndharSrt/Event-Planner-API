package com.app.events.web.controller;

import com.app.events.config.MongoConfig;
import com.app.events.model.Guest;
import com.app.events.service.GuestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.data.domain.PageImpl;

@WebMvcTest(value = GuestController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
}, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MongoConfig.class))
class GuestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GuestService guestService;

    @Autowired
    private ObjectMapper objectMapper;

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
    void createGuest_shouldReturnCreatedGuest() throws Exception {
        when(guestService.createGuest(any(Guest.class))).thenReturn(guest);

        mockMvc.perform(post("/api/guests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(guest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("gst-1"))
                .andExpect(jsonPath("$.data.email").value("john@example.com"));
    }

    @Test
    void getAllGuests_shouldReturnListOfGuests() throws Exception {
        when(guestService.getAllGuests(anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(Collections.singletonList(guest)));

        mockMvc.perform(get("/api/guests")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value("gst-1"));
    }

    @Test
    void getGuestById_shouldReturnGuest() throws Exception {
        when(guestService.getGuestById("gst-1")).thenReturn(Optional.of(guest));

        mockMvc.perform(get("/api/guests/gst-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("gst-1"));
    }

    @Test
    void getGuestById_shouldReturnNotFound_whenGuestDoesNotExist() throws Exception {
        when(guestService.getGuestById("gst-999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/guests/gst-999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateGuest_shouldReturnUpdatedGuest() throws Exception {
        guest.setFirstName("Jane");
        when(guestService.updateGuest(eq("gst-1"), any(Guest.class))).thenReturn(guest);

        mockMvc.perform(patch("/api/guests/gst-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(guest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("Jane"));
    }

    @Test
    void deleteGuest_shouldReturnSuccess() throws Exception {
        doNothing().when(guestService).deleteGuest("gst-1");

        mockMvc.perform(delete("/api/guests/gst-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Guest deleted successfully"));
    }
}
