package com.app.events.service.impl;

import com.app.events.dto.BudgetSummary;
import com.app.events.dto.CategorySpent;
import com.app.events.dto.CategorySpentSummary;
import com.app.events.dto.BudgetUpdateRequest;
import com.app.events.mapper.EventBudgetMapper;
import com.app.events.model.BudgetCategory;
import com.app.events.model.EventBudget;
import com.app.events.model.Expense;
import com.app.events.model.Notification;
import com.app.events.model.enums.AlertCode;
import com.app.events.repository.EventBudgetRepository;
import com.app.events.repository.ExpenseRepository;
import com.app.events.repository.NotificationRepository;
import com.app.events.service.BudgetService;
import com.app.events.service.NotificationService;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {
    private final EventBudgetRepository eventBudgetRepository;
    private final ExpenseRepository expenseRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final MongoTemplate mongoTemplate;
    private final EventBudgetMapper eventBudgetMapper;

    // Extracted internal method to reuse if needed or just keep original structure.
    // Actually, sticking to original structure and adding checkBudgetAlerts helper
    // is cleaner.
    // Reverting getBudgetSummaryByEventId change above to keep it simple, just
    // injecting deps and adding helper.
    // See below for checkBudgetAlerts and calls.

    private void checkBudgetAlerts(String eventId) {
        Optional<EventBudget> budgetOpt = eventBudgetRepository.findByEventId(eventId);
        if (budgetOpt.isEmpty())
            return;
        EventBudget budget = budgetOpt.get();

        Map<String, Double> categorySpentMap = calculateSpentByCategoryForEvent(eventId);
        double totalSpent = categorySpentMap.values().stream().mapToDouble(Double::doubleValue).sum();
        double totalBudget = budget.getTotalBudget();

        if (totalBudget > 0) {
            // B1EA: >= 100%
            if (totalSpent >= totalBudget) {
                if (!notificationRepository.existsByEventIdAndCodeAndReadFalse(eventId,
                        AlertCode.B1EA.getCode())) {
                    notificationService.createAlert(AlertCode.B1EA, null, eventId,
                            String.format("Spent: %.2f / %.2f", totalSpent, totalBudget));
                }
            } else {
                // Resolve B1EA if exists
                List<Notification> alerts = notificationRepository
                        .findByEventIdAndCodeAndReadFalse(eventId, AlertCode.B1EA.getCode());
                alerts.forEach(a -> notificationService.closeAlert(a.getId()));
            }

            // BOVA: > 100% (Let's say meaningful overrun, but strict requirement is type
            // BOVA)
            // If we treat BOVA as same condition as B1EA but Critical, maybe just trigger
            // both?
            // Or BOVA when > 100% (Strictly >). B1EA when == 100%?
            // Usually "100% Exceed" means >= 100%. "Overrun" means > 100%.
            if (totalSpent > totalBudget) {
                if (!notificationRepository.existsByEventIdAndCodeAndReadFalse(eventId,
                        AlertCode.BOVA.getCode())) {
                    notificationService.createAlert(AlertCode.BOVA, null, eventId,
                            String.format("Overrun by %.2f", totalSpent - totalBudget));
                }
            } else {
                // Resolve BOVA
                List<Notification> alerts = notificationRepository
                        .findByEventIdAndCodeAndReadFalse(eventId, AlertCode.BOVA.getCode());
                alerts.forEach(a -> notificationService.closeAlert(a.getId()));
            }
        }
    }

    @Override
    public BudgetSummary getBudgetSummaryByEventId(String eventId) {
        EventBudget budget = eventBudgetRepository.findByEventId(eventId)
                .orElseThrow(() -> new RuntimeException("Budget not found for event: " + eventId));

        // Calculate spent amounts using aggregation
        Map<String, Double> categorySpentMap = calculateSpentByCategoryForEvent(eventId);

        double totalSpent = categorySpentMap.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        // Build category summaries with calculated spent amounts
        List<CategorySpentSummary> categorySummaries = budget.getCategories().stream()
                .map(category -> {
                    double spentAmount = categorySpentMap.getOrDefault(category.getId(), 0.0);
                    return new CategorySpentSummary(
                            category.getId(),
                            category.getName(),
                            category.getAllocatedAmount(),
                            spentAmount,
                            category.getColor(),
                            category.getIcon());
                })
                .collect(Collectors.toList());

        return new BudgetSummary(
                budget.getId(),
                budget.getEventId(),
                budget.getTotalBudget(),
                totalSpent,
                budget.getCurrency(),
                categorySummaries);
    }

    @Override
    public Optional<EventBudget> getEventBudgetByEventId(String eventId) {
        return eventBudgetRepository.findByEventId(eventId);
    }

    @Override
    public EventBudget upsertBudget(String eventId, BudgetUpdateRequest budgetUpdates) {
        Optional<EventBudget> existing = eventBudgetRepository.findByEventId(eventId);

        if (existing.isPresent()) {
            EventBudget b = existing.get();
            eventBudgetMapper.updateBudgetFromDto(budgetUpdates, b);
            EventBudget saved = eventBudgetRepository.save(b);
            checkBudgetAlerts(eventId);
            return saved;
        }

        EventBudget newBudget = eventBudgetMapper.toEntity(budgetUpdates);
        newBudget.setEventId(eventId);
        EventBudget saved = eventBudgetRepository.save(newBudget);
        checkBudgetAlerts(eventId);
        return saved;
    }

    @Override
    public List<Expense> getExpensesByEventId(String eventId) {
        return expenseRepository.findByEventId(eventId);
    }

    @Override
    public List<Expense> getExpensesByCategoryId(String categoryId) {
        return expenseRepository.findByCategoryId(categoryId);
    }

    @Override
    public Expense addExpense(Expense expense) {
        // Validate that the event budget exists
        eventBudgetRepository.findByEventId(expense.getEventId())
                .orElseThrow(() -> new RuntimeException("Budget not found for event: " + expense.getEventId()));

        // Simply insert the expense - no manual spent amount updates
        return expenseRepository.save(expense);
    }

    @Override
    public Expense updateExpense(String id, Expense updates) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));

        // Update fields
        if (updates.getAmount() != 0) {
            expense.setAmount(updates.getAmount());
        }
        if (updates.getDescription() != null) {
            expense.setDescription(updates.getDescription());
        }
        if (updates.getDate() != null) {
            expense.setDate(updates.getDate());
        }
        if (updates.getCategoryId() != null) {
            expense.setCategoryId(updates.getCategoryId());
        }
        if (updates.getStatus() != null) {
            expense.setStatus(updates.getStatus());
        }
        if (updates.getVendor() != null) {
            expense.setVendor(updates.getVendor());
        }
        if (updates.getNotes() != null) {
            expense.setNotes(updates.getNotes());
        }

        // Simply save the expense - spent amount will be recalculated via aggregation
        return expenseRepository.save(expense);
    }

    @Override
    public void deleteExpense(String id) {
        // Simply delete the expense - spent amount will be recalculated via aggregation
        Expense e = expenseRepository.findById(id).orElse(null);
        String eventId = e != null ? e.getEventId() : null;
        expenseRepository.deleteById(id);
        if (eventId != null) {
            checkBudgetAlerts(eventId);
        }
    }

    @Override
    public EventBudget addCategory(String eventBudgetId, BudgetCategory category) {
        EventBudget budget = eventBudgetRepository.findById(eventBudgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found with id: " + eventBudgetId));

        // Generate MongoDB ObjectId for the new category if not provided
        if (category.getId() == null || category.getId().isEmpty()) {
            category.setId(new ObjectId().toString());
        }

        // Add the category to the list
        budget.getCategories().add(category);

        // Save and return the updated budget
        return eventBudgetRepository.save(budget);
    }

    @Override
    public EventBudget updateCategory(String eventBudgetId, String categoryId, BudgetCategory categoryUpdates) {
        EventBudget budget = eventBudgetRepository.findById(eventBudgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found with id: " + eventBudgetId));

        // Find the category in the list
        BudgetCategory existingCategory = budget.getCategories().stream()
                .filter(cat -> cat.getId().equals(categoryId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));

        // Update fields if provided
        if (categoryUpdates.getName() != null) {
            existingCategory.setName(categoryUpdates.getName());
        }
        if (categoryUpdates.getAllocatedAmount() != 0) {
            existingCategory.setAllocatedAmount(categoryUpdates.getAllocatedAmount());
        }
        if (categoryUpdates.getColor() != null) {
            existingCategory.setColor(categoryUpdates.getColor());
        }
        if (categoryUpdates.getIcon() != null) {
            existingCategory.setIcon(categoryUpdates.getIcon());
        }

        // Save and return the updated budget
        return eventBudgetRepository.save(budget);
    }

    @Override
    public void deleteCategory(String eventBudgetId, String categoryId) {
        EventBudget budget = eventBudgetRepository.findById(eventBudgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found with id: " + eventBudgetId));

        // Remove the category from the list
        budget.getCategories().removeIf(category -> category.getId().equals(categoryId));

        // Save the updated budget
        eventBudgetRepository.save(budget);
    }

    /**
     * Calculate spent amounts per category for a given event using MongoDB
     * aggregation
     */
    private Map<String, Double> calculateSpentByCategoryForEvent(String eventId) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("eventId").is(eventId)),
                Aggregation.group("categoryId").sum("amount").as("totalSpent"),
                Aggregation.project("totalSpent").and("_id").as("categoryId"));

        AggregationResults<CategorySpent> results = mongoTemplate.aggregate(
                aggregation,
                "expenses",
                CategorySpent.class);

        return results.getMappedResults().stream()
                .collect(Collectors.toMap(
                        CategorySpent::getCategoryId,
                        CategorySpent::getTotalSpent,
                        Double::sum)); // Merge function to handle duplicate keys (e.g., null categoryId)
    }

}
