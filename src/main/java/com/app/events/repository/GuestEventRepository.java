package com.app.events.repository;

import com.app.events.model.GuestEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuestEventRepository extends MongoRepository<GuestEvent, String> {
    List<GuestEvent> findByEventId(String eventId);

    List<GuestEvent> findByGuestId(String guestId);
}
