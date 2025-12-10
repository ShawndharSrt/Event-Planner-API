# Auto-Incrementing UserId Implementation

## Overview
Successfully implemented auto-incrementing `userId` field for the User model with format **AB00001**, **AB00002**, etc.

## Changes Made

### 1. User Model (`User.java`)
- Added `userId` field (String type)
- This field is auto-generated and separate from MongoDB's `_id`

### 2. Sequence Model (`Sequence.java`)
- New model to track auto-increment counters
- Stored in `sequence` collection
- Contains `id` (sequence name) and `seq` (current counter value)

### 3. Sequence Generator Service
- **Interface**: `SequenceGeneratorService.java`
- **Implementation**: `SequenceGeneratorServiceImpl.java`
- Uses MongoDB's `findAndModify` operation for atomic increment
- Thread-safe and handles concurrent requests

### 4. UserServiceImpl Updates
- Injected `SequenceGeneratorService`
- Modified `createUser()` method to:
  1. Generate next sequence number
  2. Format as `AB%05d` (e.g., AB00001)
  3. Set userId before saving

### 5. Sample Data Updates (`mongo_sample_data.js`)
- Added `userId` field to sample users (AB00001, AB00002)
- Added sequence counter initialization
- Sequence starts at 2 (since we have 2 sample users)

## How It Works

1. When a new user is created via `createUser()`:
   ```java
   long sequence = sequenceGeneratorService.generateSequence("user_sequence");
   String userId = String.format("AB%05d", sequence);
   user.setUserId(userId);
   ```

2. The sequence generator atomically increments the counter in MongoDB:
   - First user: sequence = 1 → userId = "AB00001"
   - Second user: sequence = 2 → userId = "AB00002"
   - Third user: sequence = 3 → userId = "AB00003"
   - And so on...

3. MongoDB's `_id` field remains unchanged and continues to work as the primary key

## Collections Structure

### User Collection
```json
{
  "_id": "650c1f1e1f1e1f1e1f1e0001",
  "userId": "AB00001",
  "name": "Alice Admin",
  "email": "alice@example.com",
  ...
}
```

### Sequence Collection
```json
{
  "_id": "user_sequence",
  "seq": 2
}
```

## Notes
- The `_id` field is NOT removed (as requested)
- `userId` is a separate, human-readable identifier
- Thread-safe implementation using MongoDB atomic operations
- Format can be easily changed by modifying the `String.format()` pattern
