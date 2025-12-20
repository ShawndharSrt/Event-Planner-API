package com.app.events.repository;

import com.app.events.model.Event;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends MongoRepository<Event, String> {
    long countByCreatedBy(String userId);

    java.util.List<Event> findByCreatedBy(String createdBy);
}
