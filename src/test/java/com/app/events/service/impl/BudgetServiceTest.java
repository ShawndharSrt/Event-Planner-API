package com.app.events.service.impl;

import com.app.events.model.Budget;
import com.app.events.model.BudgetCategory;
import com.app.events.model.Expense;
import com.app.events.repository.BudgetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    private Budget budget;
    private Expense expense;

    @BeforeEach
    void setUp() {
        budget = new Budget();
        budget.setId("bdg-1");
        budget.setEventId("evt-1");
        budget.setTotalBudget(1000.0);
        budget.setSpent(0.0);
        budget.setExpenses(new ArrayList<>());
        budget.setCategories(new ArrayList<>());

        expense = new Expense();
        expense.setId("exp-1");
        expense.setAmount(100.0);
        expense.setEventId("evt-1");
        expense.setCategoryId("cat-1");
    }

    @Test
    void upsertBudget_shouldCreateNewBudget_whenNoneExists() {
        when(budgetRepository.findByEventId("evt-1")).thenReturn(Optional.empty());
        when(budgetRepository.save(any(Budget.class))).thenReturn(budget);

        Budget result = budgetService.upsertBudget("evt-1", budget);

        assertNotNull(result);
        verify(budgetRepository).save(any(Budget.class));
    }

    @Test
    void upsertBudget_shouldUpdateExistingBudget_whenExists() {
        when(budgetRepository.findByEventId("evt-1")).thenReturn(Optional.of(budget));
        when(budgetRepository.save(any(Budget.class))).thenReturn(budget);

        Budget update = new Budget();
        update.setTotalBudget(5000.0);

        Budget result = budgetService.upsertBudget("evt-1", update);

        assertNotNull(result);
        verify(budgetRepository).save(budget);
        // Logic inside upsert updates the existing object's fields
    }

    @Test
    void addExpense_shouldUpdateBudgetSpent() {
        when(budgetRepository.findByEventId("evt-1")).thenReturn(Optional.of(budget));

        budgetService.addExpense(expense);

        assertEquals(100.0, budget.getSpent());
        assertEquals(1, budget.getExpenses().size());
        verify(budgetRepository).save(budget);
    }

    @Test
    void updateExpense_shouldRecalculateSpent() {
        budget.getExpenses().add(expense);
        budget.setSpent(100.0);

        when(budgetRepository.findByExpenseId("exp-1")).thenReturn(Optional.of(budget));

        Expense updates = new Expense();
        updates.setAmount(200.0); // Increasing by 100

        budgetService.updateExpense("exp-1", updates);

        assertEquals(200.0, budget.getSpent()); // 100 + (200 - 100) = 200
        verify(budgetRepository).save(budget);
    }

    @Test
    void deleteExpense_shouldReduceSpent() {
        budget.getExpenses().add(expense);
        budget.setSpent(100.0);

        when(budgetRepository.findByExpenseId("exp-1")).thenReturn(Optional.of(budget));

        budgetService.deleteExpense("exp-1");

        assertEquals(0.0, budget.getSpent());
        assertTrue(budget.getExpenses().isEmpty());
        verify(budgetRepository).save(budget);
    }

    @Test
    void getCategoryById_shouldReturnCategory() {
        BudgetCategory cat = new BudgetCategory();
        cat.setId("cat-1");
        budget.getCategories().add(cat);

        when(budgetRepository.findByCategoryId("cat-1")).thenReturn(Optional.of(budget));

        Optional<BudgetCategory> found = budgetService.getCategoryById("cat-1");

        assertTrue(found.isPresent());
        assertEquals("cat-1", found.get().getId());
    }
}
