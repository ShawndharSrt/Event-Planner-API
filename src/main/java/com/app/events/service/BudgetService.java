package com.app.events.service;

import com.app.events.dto.BudgetSummary;
import com.app.events.dto.BudgetUpdateRequest;
import com.app.events.model.BudgetCategory;
import com.app.events.model.EventBudget;
import com.app.events.model.Expense;

import java.util.List;
import java.util.Optional;

public interface BudgetService {
    BudgetSummary getBudgetSummaryByEventId(String eventId);

    Optional<EventBudget> getEventBudgetByEventId(String eventId);

    EventBudget upsertBudget(String eventId, BudgetUpdateRequest budgetUpdates);

    List<Expense> getExpensesByEventId(String eventId);

    List<Expense> getExpensesByCategoryId(String categoryId);

    Expense addExpense(Expense expense);

    Expense updateExpense(String id, Expense expense);

    void deleteExpense(String id);

    EventBudget addCategory(String eventBudgetId, BudgetCategory category);

    EventBudget updateCategory(String eventBudgetId, String categoryId, BudgetCategory categoryUpdates);

    void deleteCategory(String eventBudgetId, String categoryId);
}
