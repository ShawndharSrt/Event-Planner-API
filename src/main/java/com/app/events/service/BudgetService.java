package com.app.events.service;

import com.app.events.model.Budget;
import java.util.List;
import java.util.Optional;

public interface BudgetService {
    Optional<Budget> getBudgetByEventId(String eventId);
    Budget upsertBudget(String eventId, Budget budget);
    List<Budget> getAllBudgetsForEvent(String eventId);
}

