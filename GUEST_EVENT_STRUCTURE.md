# Guest and GuestEvent Collections - Implementation Guide

## Overview
The guest management system has been refactored to use a **normalized database structure** with two separate collections:
1. **Guest** - Stores guest information (name, email, phone)
2. **GuestEvent** - Junction table linking guests to events with event-specific data (group, status, dietary, notes)

This allows:
- A guest to attend multiple events
- Event-specific information (like dietary preferences for a specific event)
- Better data normalization and reduced redundancy

## Database Schema

### Guest Collection (`guest`)
```json
{
  "_id": "650c1f1e1f1e1f1e1f1e0021",
  "guestId": "G00001",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phone": "+1234567890",
  "created_at": "2025-12-07T11:40:00.000Z",
  "updated_at": "2025-12-07T11:40:00.000Z"
}
```

**Fields:**
- `_id`: MongoDB ObjectId (primary key)
- `guestId`: Human-readable guest ID (e.g., G00001, G00002)
- `firstName`: Guest's first name
- `lastName`: Guest's last name
- `email`: Guest's email address
- `phone`: Guest's phone number

### GuestEvent Collection (`guest_event`)
```json
{
  "_id": "650c1f1e1f1e1f1e1f1e0071",
  "guestId": "650c1f1e1f1e1f1e1f1e0021",
  "eventId": "650c1f1e1f1e1f1e1f1e0011",
  "group": "Family",
  "status": "CONFIRMED",
  "dietary": "Vegetarian",
  "notes": "Plus one",
  "created_at": "2025-12-07T11:40:00.000Z",
  "updated_at": "2025-12-07T11:40:00.000Z"
}
```

**Fields:**
- `_id`: MongoDB ObjectId (primary key)
- `guestId`: Reference to Guest._id (foreign key)
- `eventId`: Reference to Event._id (foreign key)
- `group`: Guest category (Family, Friends, Colleagues, etc.)
- `status`: RSVP status (CONFIRMED, PENDING, DECLINED)
- `dietary`: Dietary restrictions for this event
- `notes`: Additional notes for this guest at this event

## API Endpoint

### GET /api/events/{id}/guests

Returns a list of guests for a specific event with joined data.

**Response:**
```json
{
  "success": true,
  "message": "Event guests fetched successfully",
  "data": [
    {
      "guestId": "G00001",
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "phone": "+1234567890",
      "group": "Family",
      "status": "CONFIRMED",
      "dietary": "Vegetarian",
      "notes": "Plus one"
    }
  ]
}
```

## Implementation Details

### Models

**Guest.java**
```java
@Document(collection = "guest")
public class Guest extends BaseEntity {
    private String guestId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
}
```

**GuestEvent.java**
```java
@Document(collection = "guest_event")
public class GuestEvent extends BaseEntity {
    private String guestId;
    private String eventId;
    private String group;
    private String status;
    private String dietary;
    private String notes;
}
```

**EventGuestResponse.java** (DTO)
```java
public class EventGuestResponse {
    // From Guest
    private String guestId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    
    // From GuestEvent
    private String group;
    private String status;
    private String dietary;
    private String notes;
}
```

### Service Layer

The `GuestServiceImpl.getEventGuestsByEventId()` method performs the join:

1. Fetch all `GuestEvent` records for the given `eventId`
2. For each `GuestEvent`, fetch the corresponding `Guest` by `guestId`
3. Combine the data into `EventGuestResponse` DTO
4. Return the list

```java
public List<EventGuestResponse> getEventGuestsByEventId(String eventId) {
    List<GuestEvent> guestEvents = guestEventRepository.findByEventId(eventId);
    
    return guestEvents.stream()
        .map(guestEvent -> {
            Optional<Guest> guestOpt = guestRepository.findById(guestEvent.getGuestId());
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
                    guestEvent.getNotes()
                );
            }
            return null;
        })
        .filter(response -> response != null)
        .collect(Collectors.toList());
}
```

## Sample Data

The `sample-data.json` file contains:
- **3 guests** (John Doe, Jane Smith, Tech Guru)
- **3 guest-event relationships**:
  - John Doe → Summer Wedding (Family, Confirmed)
  - Jane Smith → Summer Wedding (Friends, Pending)
  - Tech Guru → Tech Conference (Speaker, Confirmed)

## Benefits of This Structure

1. **Reusability**: Same guest can attend multiple events
2. **Data Integrity**: Guest information stored once, referenced multiple times
3. **Flexibility**: Event-specific data (dietary, notes) separated from core guest info
4. **Scalability**: Easy to add new relationships without duplicating guest data
5. **Maintainability**: Update guest email once, reflects across all events

## Migration Notes

If you have existing data in the old structure (with `eventId` in Guest), you'll need to:
1. Extract unique guests (by email or name)
2. Create Guest records
3. Create GuestEvent records linking guests to events
4. Migrate event-specific fields to GuestEvent

## Files Created/Modified

### New Files:
- `model/GuestEvent.java` - Junction table model
- `dto/EventGuestResponse.java` - Response DTO for joined data
- `repository/GuestEventRepository.java` - Repository for guest-event relationships

### Modified Files:
- `model/Guest.java` - Removed eventId and event-specific fields
- `repository/GuestRepository.java` - Removed findByEventId method
- `service/GuestService.java` - Added getEventGuestsByEventId method
- `service/impl/GuestServiceImpl.java` - Implemented join logic
- `web/controller/EventController.java` - Updated endpoint to return EventGuestResponse
- `src/sample-data.json` - Restructured with separate collections
