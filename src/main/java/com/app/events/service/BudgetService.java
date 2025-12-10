package com.app.events.service;

import com.app.events.model.Budget;
import com.app.events.model.BudgetCategory;
import com.app.events.model.Expense;
import java.util.List;
import java.util.Optional;

public interface BudgetService {
    Optional<Budget> getBudgetByEventId(String eventId);

    Budget upsertBudget(String eventId, Budget budget);

    List<Budget> getAllBudgetsForEvent(String eventId);

    Expense addExpense(Expense expense);

    Expense updateExpense(String id, Expense expense);

    void deleteExpense(String id);

    Optional<BudgetCategory> getCategoryById(String id);
}
