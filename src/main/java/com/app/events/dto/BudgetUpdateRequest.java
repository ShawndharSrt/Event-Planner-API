package com.app.events.dto;

import com.app.events.model.BudgetCategory;
import lombok.Data;

import java.util.List;

@Data
public class BudgetUpdateRequest {
    private Double totalBudget;
    private String currency;
    private List<BudgetCategory> categories;
}
