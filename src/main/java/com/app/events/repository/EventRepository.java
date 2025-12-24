package com.app.events.repository;

import com.app.events.model.Event;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends MongoRepository<Event, String> {
    long countByCreatedBy(String userId);

    List<Event> findByCreatedBy(String createdBy);

    Optional<Event> findByTitle(String title);
}
