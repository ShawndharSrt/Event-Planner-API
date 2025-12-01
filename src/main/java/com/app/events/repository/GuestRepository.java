package com.app.events.repository;

import com.app.events.model.Guest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuestRepository extends MongoRepository<Guest, String> {
    List<Guest> findByEventId(String eventId);
}
