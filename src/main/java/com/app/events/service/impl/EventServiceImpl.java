package com.app.events.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.app.events.dto.BudgetSummary;
import com.app.events.dto.EventStats;
import com.app.events.dto.EventWithStats;
import com.app.events.dto.GuestExcelDto;
import com.app.events.dto.GuestImportResponse;
import com.app.events.dto.RecentEvent;
import com.app.events.dto.TimelineItem;
import com.app.events.mapper.EventGuestMapper;
import com.app.events.mapper.EventWithStatsMapper;
import com.app.events.mapper.RecentEventMapper;
import com.app.events.model.Event;
import com.app.events.model.Guest;
import com.app.events.model.enums.AlertCode;
import com.app.events.repository.EventRepository;
import com.app.events.repository.GuestRepository;
import com.app.events.service.BudgetService;
import com.app.events.service.EmailService;
import com.app.events.service.EventService;
import com.app.events.service.NotificationService;
import com.app.events.service.WhatsAppService;
import com.app.events.service.excel.GuestImportListener;
import com.app.events.util.AppUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import java.util.stream.Collectors;
import com.app.events.repository.UserRepository;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final RecentEventMapper recentEventMapper;
    private final EventWithStatsMapper eventWithStatsMapper;
    private final EventGuestMapper eventGuestMapper;
    private final BudgetService budgetService;
    private final GuestRepository guestRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final WhatsAppService whatsappService;
    private final UserRepository userRepository;

    @Override
    public List<Event> getAllEvents() {
        String currentUserId = AppUtils.getCurrentUserId();
        List<Event> events;
        if (isAdmin(currentUserId)) {
            events = eventRepository.findAll();
        } else {
            events = eventRepository.findByCreatedBy(currentUserId);
        }
        populateCreatedByName(events);
        return events;
    }

    @Override
    public List<EventWithStats> getAllEventsWithStats() {
        String currentUserId = AppUtils.getCurrentUserId();
        List<Event> events;
        if (isAdmin(currentUserId)) {
            events = eventRepository.findAll();
        } else {
            events = eventRepository.findByCreatedBy(currentUserId);
        }
        populateCreatedByName(events);
        return events.stream().map(event -> eventWithStatsMapper.toEventWithStats(event, getEventStats(event.getId())))
                .collect(Collectors.toList());
    }

    private void populateCreatedByName(List<? extends Event> events) {
        Set<String> userIds = events.stream()
                .map(Event::getCreatedBy)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (userIds.isEmpty()) {
            return;
        }

        Map<String, String> userNames = new HashMap<>();
        userRepository.findByUserIdIn(userIds).forEach(user -> {
            String fullName = (user.getFirstName() != null ? user.getFirstName() : "") + " "
                    + (user.getLastName() != null ? user.getLastName() : "");
            userNames.put(user.getUserId(), fullName.trim());
        });

        events.forEach(event -> {
            if (event.getCreatedBy() != null) {
                event.setCreatedByName(userNames.getOrDefault(event.getCreatedBy(), "Unknown User"));
            }
        });
    }

    @Override
    public Optional<Event> getEventById(String id) {
        String currentUserId = AppUtils.getCurrentUserId();
        return eventRepository.findById(id)
                .filter(event -> isAdmin(currentUserId) || currentUserId.equals(event.getCreatedBy()));
    }

    @Override
    public Event createEvent(Event event) {
        event.setCreatedBy(AppUtils.getCurrentUserId());
        return eventRepository.save(event);
    }

    @Override
    public Event updateEvent(String id, Event event) {
        String currentUserId = AppUtils.getCurrentUserId();
        return eventRepository.findById(id).map(existing -> {
            if (!isAdmin(currentUserId) && !currentUserId.equals(existing.getCreatedBy())) {
                throw new RuntimeException("Unauthorized to update this event");
            }
            event.setId(id);
            // Preserve fields that might not be sent from the frontend during an update
            event.setCreatedBy(existing.getCreatedBy());
            event.setCreatedAt(existing.getCreatedAt());

            if (event.getCoverImage() == null) {
                event.setCoverImage(existing.getCoverImage());
            }

            if (event.getGuests() == null || event.getGuests().isEmpty()) {
                event.setGuests(existing.getGuests());
            }

            return eventRepository.save(event);
        }).orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
    }

    @Override
    public void deleteEvent(String id) {
        String currentUserId = AppUtils.getCurrentUserId();
        Event existing = eventRepository.findById(id).orElseThrow(() -> new RuntimeException("Event not found"));
        if (!isAdmin(currentUserId) && !currentUserId.equals(existing.getCreatedBy())) {
            throw new RuntimeException("Unauthorized to delete this event");
        }
        eventRepository.deleteById(id);
    }

    @Override
    public List<RecentEvent> getRecentEvents(int limit) {
        String currentUserId = AppUtils.getCurrentUserId();
        Sort sort = Sort.by(Sort.Direction.DESC, "startDate");
        List<Event> events;
        if (isAdmin(currentUserId)) {
            events = eventRepository.findAll(sort);
        } else {
            events = eventRepository.findByCreatedBy(currentUserId, sort);
        }
        return recentEventMapper.toRecentEventList(events).stream().limit(Math.max(limit, 0)).toList();
    }

    @Override
    public EventStats getEventStats(String eventId) {
        return eventRepository.findById(eventId).map(event -> {
            List<Event.EventGuest> guests = event.getGuests();
            int totalGuests = guests.size();
            int confirmed = (int) guests.stream()
                    .filter(g -> "confirmed".equalsIgnoreCase(g.getStatus())).count();
            int pending = (int) guests.stream()
                    .filter(g -> "pending".equalsIgnoreCase(g.getStatus())).count();
            int declined = (int) guests.stream()
                    .filter(g -> "declined".equalsIgnoreCase(g.getStatus())).count();
            return new EventStats(totalGuests, confirmed, pending, declined);
        }).orElse(new EventStats(0, 0, 0, 0));
    }

    @Override
    public Event addGuestsToEvent(String eventId, List<String> guestIds) {
        Event event = getEventById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));
        if (guestIds == null || guestIds.isEmpty()) {
            return event;
        }
        List<Guest> guests = guestRepository.findAllById(guestIds);

        for (Guest masterGuest : guests) {
            Optional<Event.EventGuest> existingGuestOpt = event.getGuests().stream()
                    .filter(g -> g.getGuestId().equals(masterGuest.getId())).findFirst();

            Event.EventGuest targetGuest;
            if (existingGuestOpt.isPresent()) {
                targetGuest = existingGuestOpt.get();
            } else {
                targetGuest = new Event.EventGuest();
                targetGuest.setGuestId(masterGuest.getId());
                targetGuest.setStatus("Pending");
                event.getGuests().add(targetGuest);
            }

            // Sync/Hydrate fields from Master Guest
            String fullName = (masterGuest.getFirstName() != null ? masterGuest.getFirstName() : "") + " "
                    + (masterGuest.getLastName() != null ? masterGuest.getLastName() : "");
            targetGuest.setName(fullName.trim());
            targetGuest.setEmail(masterGuest.getEmail());
            targetGuest.setGroup(masterGuest.getGroup());
            targetGuest.setDietary(masterGuest.getDietary());
            targetGuest.setNotes(masterGuest.getNotes());
            targetGuest.setPhone(masterGuest.getPhone());
        }
        Event saved = eventRepository.save(event);

        sendInvitations(saved, guests);

        // Alert EITA: "Invitation Triggered Alert" for added guests
        if (!guestIds.isEmpty()) {
            // We could check if guests were actually added (vs existing), but for MVP just
            // alert on action.
            notificationService.createAlert(AlertCode.EITA, null, eventId, "(" + guestIds.size() + " guests invited)");
        }
        return saved;
    }

    @Override
    public Event removeGuestFromEvent(String eventId, String guestId) {
        Event event = getEventById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));
        event.getGuests().removeIf(g -> g.getGuestId().equals(guestId));
        return eventRepository.save(event);
    }

    @Override
    public Event updateGuestStatus(String eventId, String guestId, String status) {
        Event event = getEventById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));
        event.getGuests().stream().filter(g -> g.getGuestId().equals(guestId)).findFirst()
                .ifPresent(g -> g.setStatus(status));
        return eventRepository.save(event);
    }

    @Override
    public List<TimelineItem> getEventTimeline(String eventId) {
        return eventRepository.findById(eventId).map(event -> {
            List<TimelineItem> timeline = new ArrayList<>();

            if (event.getStartTime() != null) {
                timeline.add(new TimelineItem(event.getStartTime(), "Event Start",
                        event.getLocation() != null ? event.getLocation() : ""));
            }

            if (event.getEndTime() != null) {
                timeline.add(new TimelineItem(event.getEndTime(), "Event End",
                        event.getLocation() != null ? event.getLocation() : ""));
            }

            return timeline;
        }).orElse(Collections.emptyList());
    }

    @Override
    public BudgetSummary getEventBudgetSummary(String eventId) {
        try {
            return budgetService.getBudgetSummaryByEventId(eventId);
        } catch (RuntimeException e) {
            return new BudgetSummary(null, eventId, 0.0, 0.0, "INR", Collections.emptyList());
        }
    }

    @Override
    @Transactional
    public GuestImportResponse importGuestsFromExcel(MultipartFile file) {
        try {
            // 1. Get all sheet names to find the one matching an event
            ExcelReader excelReader = EasyExcel.read(file.getInputStream()).build();
            List<ReadSheet> sheets = excelReader.excelExecutor().sheetList();

            String matchingSheetName = null;
            Event targetEvent = null;

            for (ReadSheet sheet : sheets) {
                // Check if this sheet name matches an event title
                Optional<Event> eventOpt = eventRepository.findByTitle(sheet.getSheetName());
                if (eventOpt.isPresent()) {
                    matchingSheetName = sheet.getSheetName();
                    targetEvent = eventOpt.get();
                    break;
                }
            }

            // Close the reader used for inspecting sheets
            excelReader.finish();

            if (targetEvent == null) {
                throw new RuntimeException("No sheet name matches an existing event title.");
            }

            // 2. Read the matching sheet
            GuestImportListener listener = new GuestImportListener();
            EasyExcel.read(file.getInputStream(), GuestExcelDto.class, listener).sheet(matchingSheetName).doRead();

            List<GuestExcelDto> importedGuests = listener.getGuests();

            // 3. Process guests
            int insertedCount = 0;
            int duplicateCount = 0;
            List<String> duplicateEmails = new ArrayList<>();

            if (targetEvent.getGuests() == null) {
                targetEvent.setGuests(new ArrayList<>());
            }

            for (GuestExcelDto dto : importedGuests) {
                // Check duplicate by email within the event
                boolean exists = targetEvent.getGuests().stream().anyMatch(g -> g.getEmail() != null
                        && dto.getEmail() != null && g.getEmail().equalsIgnoreCase(dto.getEmail()));

                if (exists) {
                    duplicateCount++;
                    duplicateEmails.add(dto.getEmail());
                } else {
                    Event.EventGuest newGuest = eventGuestMapper.toEventGuest(dto);
                    targetEvent.getGuests().add(newGuest);
                    insertedCount++;
                }
            }

            eventRepository.save(targetEvent);

            return new GuestImportResponse("Guest import completed", targetEvent.getTitle(), insertedCount,
                    duplicateCount, duplicateEmails);

        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel file", e);
        }
    }

    private void sendInvitations(Event event, List<Guest> guests) {
        String eventName = event.getTitle() != null ? event.getTitle() : "Our Event";
        String date = event.getStartDate() != null ? event.getStartDate().toString() : "TBD";
        String location = event.getLocation() != null ? event.getLocation() : "TBD";
        String hostName = "Event Host"; // Placeholder as host name is not directly on Event entity

        for (Guest guest : guests) {
            String guestName = (guest.getFirstName() != null ? guest.getFirstName() : "") + " "
                    + (guest.getLastName() != null ? guest.getLastName() : "");
            guestName = guestName.trim();
            if (guestName.isEmpty()) {
                guestName = "Guest";
            }

            String message = String.format(
                    "Hello %s,\n\nYou are invited to %s!\nDate: %s\nLocation: %s\n\nBest regards,\n%s",
                    guestName, eventName, date, location, hostName);

            String subject = "Invitation to " + eventName;

            // Send WhatsApp
            if (guest.getPhone() != null && !guest.getPhone().trim().isEmpty()) {
                try {
                    whatsappService.sendMessage(guest.getPhone(), message);
                } catch (Exception e) {
                    log.error("Failed to send WhatsApp invitation to {}", guest.getPhone(), e);
                }
            }

            // Send Email
            if (guest.getEmail() != null && !guest.getEmail().trim().isEmpty()) {
                try {
                    emailService.sendSimpleMessage(guest.getEmail(), subject, message);
                } catch (Exception e) {
                    log.error("Failed to send Email invitation to {}", guest.getEmail(), e);
                }
            }
        }
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
