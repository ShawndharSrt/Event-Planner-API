package com.app.events.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetSummary {
    private String id;
    private String eventId;
    private double totalBudget;
    private double totalSpent;
    private String currency;
    private List<CategorySpentSummary> categories;
}
