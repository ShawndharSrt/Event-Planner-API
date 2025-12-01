package com.app.events.repository;

import com.app.events.model.Budget;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.List;

public interface BudgetRepository extends MongoRepository<Budget, String> {
    Optional<Budget> findByEventId(String eventId);
    List<Budget> findAllByEventId(String eventId);
}

