package com.app.events.service.impl;

import com.app.events.dto.BudgetSummary;
import com.app.events.model.BudgetCategory;
import com.app.events.model.EventBudget;
import com.app.events.model.Expense;
import com.app.events.repository.EventBudgetRepository;
import com.app.events.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private EventBudgetRepository eventBudgetRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private com.app.events.mapper.EventBudgetMapper eventBudgetMapper;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    private EventBudget eventBudget;
    private Expense expense;
    private BudgetCategory category;

    @BeforeEach
    void setUp() {
        eventBudget = new EventBudget();
        eventBudget.setId("bdg-1");
        eventBudget.setEventId("evt-1");
        eventBudget.setTotalBudget(1000.0);
        eventBudget.setCurrency("USD");

        category = new BudgetCategory();
        category.setId("1");
        category.setName("Venue");
        category.setAllocatedAmount(500.0);
        category.setColor("#FF5733");
        category.setIcon("venue");

        List<BudgetCategory> categories = new ArrayList<>();
        categories.add(category);
        eventBudget.setCategories(categories);

        expense = new Expense();
        expense.setId("exp-1");
        expense.setAmount(100.0);
        expense.setEventId("evt-1");
        expense.setCategoryId(category.getId());
        expense.setDate(new Date());
        expense.setStatus("paid");
    }

    @Test
    void upsertBudget_shouldCreateNewBudget_whenNoneExists() {
        when(eventBudgetRepository.findByEventId("evt-1")).thenReturn(Optional.empty());
        when(eventBudgetRepository.save(any(EventBudget.class))).thenReturn(eventBudget);
        when(eventBudgetMapper.toEntity(any(com.app.events.dto.BudgetUpdateRequest.class))).thenReturn(eventBudget);

        com.app.events.dto.BudgetUpdateRequest request = new com.app.events.dto.BudgetUpdateRequest();
        request.setTotalBudget(1000.0);
        request.setCurrency("USD");

        EventBudget result = budgetService.upsertBudget("evt-1", request);

        assertNotNull(result);
        verify(eventBudgetRepository).save(any(EventBudget.class));
        verify(eventBudgetMapper).toEntity(request);
    }

    @Test
    void upsertBudget_shouldUpdateExistingBudget_whenExists() {
        when(eventBudgetRepository.findByEventId("evt-1")).thenReturn(Optional.of(eventBudget));
        when(eventBudgetRepository.save(any(EventBudget.class))).thenReturn(eventBudget);

        com.app.events.dto.BudgetUpdateRequest update = new com.app.events.dto.BudgetUpdateRequest();
        update.setTotalBudget(5000.0);

        EventBudget result = budgetService.upsertBudget("evt-1", update);

        assertNotNull(result);
        verify(eventBudgetRepository).save(eventBudget);
        verify(eventBudgetMapper).updateBudgetFromDto(update, eventBudget);
    }

    @Test
    void addExpense_shouldSaveExpense() {
        when(eventBudgetRepository.findByEventId("evt-1")).thenReturn(Optional.of(eventBudget));
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);

        Expense result = budgetService.addExpense(expense);

        assertNotNull(result);
        verify(expenseRepository).save(expense);
        // No manual spent update
        verify(eventBudgetRepository, never()).save(any());
    }

    @Test
    void updateExpense_shouldUpdateExpenseOnly() {
        when(expenseRepository.findById("exp-1")).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);

        Expense updates = new Expense();
        updates.setAmount(200.0);
        updates.setDescription("Updated description");

        Expense result = budgetService.updateExpense("exp-1", updates);

        assertNotNull(result);
        assertEquals(200.0, result.getAmount());
        verify(expenseRepository).save(expense);
        // No manual spent recalculation
    }

    @Test
    void deleteExpense_shouldDeleteExpenseOnly() {
        doNothing().when(expenseRepository).deleteById("exp-1");

        budgetService.deleteExpense("exp-1");

        verify(expenseRepository).deleteById("exp-1");
        // No manual spent reduction
    }

    @Test
    void getBudgetSummaryByEventId_shouldReturnSummaryWithCalculatedSpent() {
        when(eventBudgetRepository.findByEventId("evt-1")).thenReturn(Optional.of(eventBudget));

        // Mock aggregation results with proper argument matchers
        AggregationResults<Object> mockResults = mock(AggregationResults.class);
        when(mockResults.getMappedResults()).thenReturn(new ArrayList<>());
        when(mongoTemplate.aggregate(
                any(org.springframework.data.mongodb.core.aggregation.Aggregation.class),
                eq("expenses"),
                any(Class.class))).thenReturn(mockResults);

        BudgetSummary summary = budgetService.getBudgetSummaryByEventId("evt-1");

        assertNotNull(summary);
        assertEquals("evt-1", summary.getEventId());
        assertEquals(1000.0, summary.getTotalBudget());
        assertEquals(1, summary.getCategories().size());
    }

    @Test
    void getExpensesByEventId_shouldReturnExpenses() {
        List<Expense> expenses = List.of(expense);
        when(expenseRepository.findByEventId("evt-1")).thenReturn(expenses);

        List<Expense> result = budgetService.getExpensesByEventId("evt-1");

        assertEquals(1, result.size());
        assertEquals("exp-1", result.get(0).getId());
    }
}
