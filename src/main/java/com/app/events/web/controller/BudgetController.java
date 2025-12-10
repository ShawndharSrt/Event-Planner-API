package com.app.events.web.controller;

import com.app.events.dto.ApiResponse;
import com.app.events.model.Budget;
import com.app.events.model.BudgetCategory;
import com.app.events.model.Expense;
import com.app.events.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budget")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BudgetController {
    private final BudgetService budgetService;

    @GetMapping("/{eventId}")
    public ResponseEntity<ApiResponse<Budget>> getBudget(@PathVariable String eventId) {
        return budgetService.getBudgetByEventId(eventId)
                .map(budget -> ResponseEntity.ok(ApiResponse.success("Budget fetched", budget)))
                .orElse(ResponseEntity.ok(ApiResponse.success("No budget found", null)));
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<ApiResponse<Budget>> updateBudget(
            @PathVariable String eventId, @RequestBody Budget budget) {
        Budget saved = budgetService.upsertBudget(eventId, budget);
        return ResponseEntity.ok(ApiResponse.success("Budget updated", saved));
    }

    @PostMapping("/{eventId}/expenses")
    public ResponseEntity<ApiResponse<Expense>> addExpense(
            @PathVariable String eventId, @RequestBody Expense expense) {
        expense.setEventId(eventId);
        return ResponseEntity.ok(ApiResponse.success("Expense added", budgetService.addExpense(expense)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Expense>> updateExpense(
            @PathVariable String id, @RequestBody Expense updates) {
        return ResponseEntity.ok(ApiResponse.success("Expense updated", budgetService.updateExpense(id, updates)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(@PathVariable String id) {
        budgetService.deleteExpense(id);
        return ResponseEntity.ok(ApiResponse.success("Expense deleted", null));
    }

    @GetMapping("/budget-categories/{id}")
    public ResponseEntity<ApiResponse<BudgetCategory>> getCategoryById(@PathVariable String id) {
        return budgetService.getCategoryById(id)
                .map(category -> ResponseEntity.ok(ApiResponse.success("Category fetched", category)))
                .orElse(ResponseEntity.notFound().build());
    }
}
