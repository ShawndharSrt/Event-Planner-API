package com.app.events.service.impl;

import com.app.events.model.Budget;
import com.app.events.model.BudgetCategory;
import com.app.events.model.Expense;
import com.app.events.repository.BudgetRepository;
import com.app.events.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {
    private final BudgetRepository budgetRepository;

    @Override
    public Optional<Budget> getBudgetByEventId(String eventId) {
        return budgetRepository.findByEventId(eventId);
    }

    @Override
    public Budget upsertBudget(String eventId, Budget budget) {
        // If budget exists, update it, otherwise create
        Optional<Budget> existing = budgetRepository.findByEventId(eventId);
        if (existing.isPresent()) {
            Budget b = existing.get();
            b.setTotalBudget(budget.getTotalBudget());
            b.setCurrency(budget.getCurrency());
            b.setNotes(budget.getNotes());
            if (budget.getSpent() > 0) {
                b.setSpent(budget.getSpent());
            }
            return budgetRepository.save(b);
        }

        budget.setEventId(eventId);
        return budgetRepository.save(budget);
    }

    @Override
    public List<Budget> getAllBudgetsForEvent(String eventId) {
        return budgetRepository.findAllByEventId(eventId);
    }

    @Override
    public Expense addExpense(Expense expense) {
        Budget budget = budgetRepository.findByEventId(expense.getEventId())
                .orElseThrow(() -> new RuntimeException("Budget not found for event: " + expense.getEventId()));

        if (budget.getExpenses() == null) {
            budget.setExpenses(new ArrayList<>());
        }

        expense.setId(UUID.randomUUID().toString()); // Generate ID
        budget.getExpenses().add(expense);

        // Update spent amount?
        // Logic: Should I update budget.spent?
        // Let's assume yes, simplistic approach.
        budget.setSpent(budget.getSpent() + expense.getAmount());

        // Also update category spentAmount
        if (budget.getCategories() != null) {
            budget.getCategories().stream()
                    .filter(c -> c.getId().equals(expense.getCategoryId()))
                    .findFirst()
                    .ifPresent(c -> c.setSpentAmount(c.getSpentAmount() + expense.getAmount()));
        }

        budgetRepository.save(budget);
        return expense;
    }

    @Override
    public Expense updateExpense(String id, Expense updates) {
        Budget budget = budgetRepository.findByExpenseId(id)
                .orElseThrow(() -> new RuntimeException("Budget not found for expense: " + id));

        return budget.getExpenses().stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .map(expense -> {
                    double oldAmount = expense.getAmount();

                    if (updates.getAmount() != 0)
                        expense.setAmount(updates.getAmount());
                    if (updates.getDescription() != null)
                        expense.setDescription(updates.getDescription());
                    if (updates.getDate() != null)
                        expense.setDate(updates.getDate());
                    if (updates.getCategoryId() != null)
                        expense.setCategoryId(updates.getCategoryId());
                    if (updates.getCategoryName() != null)
                        expense.setCategoryName(updates.getCategoryName());
                    if (updates.getStatus() != null)
                        expense.setStatus(updates.getStatus());
                    if (updates.getVendor() != null)
                        expense.setVendor(updates.getVendor());
                    if (updates.getNotes() != null)
                        expense.setNotes(updates.getNotes());

                    // Update budget spent if amount changed
                    if (updates.getAmount() != 0) {
                        double diff = updates.getAmount() - oldAmount;
                        budget.setSpent(budget.getSpent() + diff);

                        // Update category spent
                        if (budget.getCategories() != null) {
                            budget.getCategories().stream()
                                    .filter(c -> c.getId().equals(expense.getCategoryId()))
                                    .findFirst()
                                    .ifPresent(c -> c.setSpentAmount(c.getSpentAmount() + diff));
                        }
                    }

                    budgetRepository.save(budget);
                    return expense;
                })
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));
    }

    @Override
    public void deleteExpense(String id) {
        Budget budget = budgetRepository.findByExpenseId(id)
                .orElseThrow(() -> new RuntimeException("Budget not found for expense: " + id));

        budget.getExpenses().stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .ifPresent(expense -> {
                    budget.setSpent(budget.getSpent() - expense.getAmount());

                    if (budget.getCategories() != null) {
                        budget.getCategories().stream()
                                .filter(c -> c.getId().equals(expense.getCategoryId()))
                                .findFirst()
                                .ifPresent(c -> c.setSpentAmount(c.getSpentAmount() - expense.getAmount()));
                    }

                    budget.getExpenses().remove(expense);
                    budgetRepository.save(budget);
                });
    }

    @Override
    public Optional<BudgetCategory> getCategoryById(String id) {
        return budgetRepository.findByCategoryId(id)
                .flatMap(budget -> budget.getCategories().stream()
                        .filter(c -> c.getId().equals(id))
                        .findFirst());
    }
}
