package com.app.events.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "budget")
public class Budget extends BaseEntity {
    private String eventId;
    private double totalBudget;
    private double spent;
    private String currency;
    private String notes;
    private List<BudgetCategory> categories = new ArrayList<>();
    private List<Expense> expenses = new ArrayList<>();
}
