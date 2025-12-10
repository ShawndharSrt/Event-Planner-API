# Guest and GuestEvent Collections - Implementation Guide

## Overview
The guest management system has been refactored to use an **embedded guest structure** within the **Event** collection.
There are now two main collections:
1. **Guest** - Stores master guest information (name, email, phone)
2. **Event** - Stores event details, including an embedded list of guests with event-specific statuses.

The **GuestEvent** collection has been **REMOVED**.

This allows:
- **Simplified Data Fetching**: Event guests are loaded with the event.
- **Atomic Updates**: Adding/removing a guest is an update to the Event document.
- **Maintainability**: Reduced number of collections and joins.

## Database Schema

### Guest Collection (`guest`)
Functions as a master list of all known guests.
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

### Event Collection (`event`)
Now includes `guests` array.
```json
{
  "_id": "650c1f1e1f1e1f1e1f1e0011",
  "title": "Summer Wedding",
  "startDate": "2025-06-15",
  "guests": [
    {
      "guestId": "650c1f1e1f1e1f1e1f1e0021", // Reference to Guest._id
      "name": "John Doe", " // Cached name
      "email": "john.doe@example.com", // Cached email
      "group": "Family",
      "status": "CONFIRMED",
      "dietary": "Vegetarian",
      "notes": "Plus one"
    }
  ]
}
```

## API Endpoints

### Guest Management
Using `EventController`:

- **GET /api/events/{id}/guests**
  - Returns list of `EventGuestResponse`.
- **POST /api/events/{id}/guests**
  - Adds a guest to the event (embedded).
  - Body: `EventGuest` object.
- **DELETE /api/events/{id}/guests/{guestId}**
  - Removes a guest from the event.
- **PATCH /api/events/{id}/guests/{guestId}**
  - Updates guest status (body: `{ "status": "..." }`).

### Service Layer

`EventService` now handles guest logic:
```java
Event addGuestToEvent(String eventId, Event.EventGuest guest);
Event removeGuestFromEvent(String eventId, String guestId);
Event updateGuestStatus(String eventId, String guestId, String status);
```

`GuestService` is now purely for managing the master `Guest` list.

## Frontend Updates Needed
- Update `Event` interface to include `guests: EventGuest[]`.
- Update `EventService` calls to use the new endpoints.
