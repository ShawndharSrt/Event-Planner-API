package com.app.events.controller;

import com.app.events.dto.ApiResponse;
import com.app.events.model.Budget;
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
}
