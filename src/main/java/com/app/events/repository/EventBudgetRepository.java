package com.app.events.repository;

import com.app.events.model.EventBudget;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface EventBudgetRepository extends MongoRepository<EventBudget, String> {
    Optional<EventBudget> findByEventId(String eventId);

    void deleteByEventId(String eventId);
}
