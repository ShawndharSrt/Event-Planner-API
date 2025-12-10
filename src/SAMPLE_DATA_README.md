# Sample Data Import Guide

This directory contains sample data for the Event Planner API MongoDB database.

## File: `sample-data.json`

Contains sample data for all collections with proper relationships:
- **sequences**: 1 record (user_sequence counter)
- **users**: 2 records (AB00001, AB00002)
- **events**: 2 records (Wedding, Conference)
- **guests**: 3 records (linked to events)
- **tasks**: 3 records (linked to events)
- **budgets**: 2 records (linked to events)
- **notifications**: 2 records (linked to users and events)
- **settings**: 2 records (linked to users)

## How to Import

### Method 1: Using MongoDB Compass (GUI)
1. Open MongoDB Compass
2. Connect to your MongoDB instance
3. Select your database
4. For each collection:
   - Click on the collection name
   - Click "ADD DATA" → "Import JSON or CSV file"
   - Select the appropriate section from `sample-data.json`
   - Import the data

### Method 2: Using mongoimport (Command Line)

First, you need to split the JSON file by collection. Here's a script to help:

```bash
# Navigate to the src directory
cd /Users/shawndharb/Projects/API/events/src

# Import each collection (you'll need to extract each array first)
# Example for users:
mongoimport --db events_db --collection user --file users.json --jsonArray

# Repeat for each collection:
# - sequence
# - user
# - event
# - guest
# - task
# - budget
# - notification
# - setting
```

### Method 3: Using MongoDB Shell (mongosh)

```javascript
// Connect to your database
use events_db

// Copy and paste each section from sample-data.json

// Example for sequences:
db.sequence.insertMany([
  {
    "_id": "user_sequence",
    "seq": 2
  }
])

// Example for users:
db.user.insertMany([
  {
    "_id": ObjectId("650c1f1e1f1e1f1e1f1e0001"),
    "userId": "AB00001",
    "name": "Alice Admin",
    ...
  },
  ...
])

// Repeat for all collections
```

### Method 4: Using the JavaScript Import Script

Use the `mongo_sample_data.js` file located in the `.gemini` directory:

```bash
mongosh events_db < /path/to/mongo_sample_data.js
```

## Data Relationships

### User IDs
- `650c1f1e1f1e1f1e1f1e0001` → Alice Admin (AB00001)
- `650c1f1e1f1e1f1e1f1e0002` → Bob Planner (AB00002)

### Event IDs
- `650c1f1e1f1e1f1e1f1e0011` → Summer Wedding 2024
- `650c1f1e1f1e1f1e1f1e0012` → Tech Conference 2024

### Relationships
- **Guests** are linked to events via `eventId`
- **Tasks** are linked to events via `eventId`
- **Budgets** are linked to events via `eventId`
- **Notifications** are linked to both users and events via `userId` and `eventId`
- **Settings** are linked to users via `userId` (and optionally to events via `eventId`)

## Collection Names

Make sure your MongoDB collections are named as follows:
- `sequence`
- `user`
- `event`
- `guest`
- `task`
- `budget`
- `notification`
- `setting`

## Notes

1. **Passwords**: The sample passwords are hashed using BCrypt. The actual passwords are not included for security.
2. **Dates**: All dates are in ISO 8601 format
3. **ObjectIds**: The `_id` fields use specific ObjectIds to maintain relationships
4. **userId**: Auto-generated field in format AB00001, AB00002, etc.
5. **Sequence Counter**: Initialized to 2 (since we have 2 sample users)

## Next User Creation

When you create the next user through the API, it will automatically get:
- `userId`: AB00003
- `_id`: Auto-generated MongoDB ObjectId

The sequence counter will automatically increment.
