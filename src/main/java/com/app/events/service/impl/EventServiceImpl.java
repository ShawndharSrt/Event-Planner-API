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
import com.app.events.service.EventService;
import com.app.events.service.NotificationService;
import com.app.events.service.excel.GuestImportListener;
import com.app.events.util.AppUtils;

import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final RecentEventMapper recentEventMapper;
    private final EventWithStatsMapper eventWithStatsMapper;
    private final EventGuestMapper eventGuestMapper;
    private final BudgetService budgetService;
    private final GuestRepository guestRepository;
    private final NotificationService notificationService;

    @Override
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @Override
    public List<EventWithStats> getAllEventsWithStats() {
        List<Event> events = eventRepository.findAll();
        return events.stream().map(event -> eventWithStatsMapper.toEventWithStats(event, getEventStats(event.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Event> getEventById(String id) {
        return eventRepository.findById(id);
    }

    @Override
    public Event createEvent(Event event) {
        event.setCreatedBy(AppUtils.getCurrentUserId());
        return eventRepository.save(event);
    }

    @Override
    public Event updateEvent(String id, Event event) {
        if (eventRepository.existsById(id)) {
            event.setId(id);
            return eventRepository.save(event);
        }
        throw new RuntimeException("Event not found with id: " + id);
    }

    @Override
    public void deleteEvent(String id) {
        eventRepository.deleteById(id);
    }

    @Override
    public List<RecentEvent> getRecentEvents(int limit) {
        List<Event> events = eventRepository.findAll(Sort.by(Sort.Direction.DESC, "startDate"));
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
        }
        Event saved = eventRepository.save(event);
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
            return new BudgetSummary(null, eventId, 0.0, 0.0, "USD", Collections.emptyList());
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
                    if (dto.getEmail() != null) {
                        duplicateEmails.add(dto.getEmail());
                    }
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
}
