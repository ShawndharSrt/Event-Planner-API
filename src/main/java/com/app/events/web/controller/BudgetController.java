package com.app.events.web.controller;

import com.app.events.dto.ApiResponse;
import com.app.events.dto.BudgetSummary;
import com.app.events.dto.BudgetUpdateRequest;
import com.app.events.model.BudgetCategory;
import com.app.events.model.EventBudget;
import com.app.events.model.Expense;
import com.app.events.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budget")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BudgetController {
    private final BudgetService budgetService;

    @GetMapping("/{eventId}")
    public ResponseEntity<ApiResponse<BudgetSummary>> getBudget(@PathVariable String eventId) {
        BudgetSummary summary = budgetService.getBudgetSummaryByEventId(eventId);
        return ResponseEntity.ok(ApiResponse.success("Budget fetched", summary));
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<ApiResponse<EventBudget>> updateBudget(
            @PathVariable String eventId, @RequestBody BudgetUpdateRequest budget) {
        EventBudget saved = budgetService.upsertBudget(eventId, budget);
        return ResponseEntity.ok(ApiResponse.success("Budget updated", saved));
    }

    @GetMapping("/{eventId}/expenses")
    public ResponseEntity<ApiResponse<List<Expense>>> getExpenses(@PathVariable String eventId) {
        List<Expense> expenses = budgetService.getExpensesByEventId(eventId);
        return ResponseEntity.ok(ApiResponse.success("Expenses fetched", expenses));
    }

    @PostMapping("/{eventId}/expenses")
    public ResponseEntity<ApiResponse<Expense>> addExpense(
            @PathVariable String eventId, @RequestBody Expense expense) {
        expense.setEventId(eventId);
        return ResponseEntity.ok(ApiResponse.success("Expense added", budgetService.addExpense(expense)));
    }

    @PatchMapping("/expenses/{id}")
    public ResponseEntity<ApiResponse<Expense>> updateExpense(
            @PathVariable String id, @RequestBody Expense updates) {
        return ResponseEntity.ok(ApiResponse.success("Expense updated", budgetService.updateExpense(id, updates)));
    }

    @DeleteMapping("/expenses/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(@PathVariable String id) {
        budgetService.deleteExpense(id);
        return ResponseEntity.ok(ApiResponse.success("Expense deleted", null));
    }

    @PostMapping("/{eventBudgetId}/categories")
    public ResponseEntity<ApiResponse<EventBudget>> addCategory(
            @PathVariable String eventBudgetId, @RequestBody BudgetCategory category) {
        EventBudget updatedBudget = budgetService.addCategory(eventBudgetId, category);
        return ResponseEntity.ok(ApiResponse.success("Category added", updatedBudget));
    }

    @PatchMapping("/{eventBudgetId}/categories/{categoryId}")
    public ResponseEntity<ApiResponse<EventBudget>> updateCategory(
            @PathVariable String eventBudgetId,
            @PathVariable String categoryId,
            @RequestBody BudgetCategory categoryUpdates) {
        EventBudget updatedBudget = budgetService.updateCategory(eventBudgetId, categoryId, categoryUpdates);
        return ResponseEntity.ok(ApiResponse.success("Category updated", updatedBudget));
    }

    @DeleteMapping("/{eventBudgetId}/categories/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable String eventBudgetId, @PathVariable String categoryId) {
        budgetService.deleteCategory(eventBudgetId, categoryId);
        return ResponseEntity.ok(ApiResponse.success("Category deleted", null));
    }
}
