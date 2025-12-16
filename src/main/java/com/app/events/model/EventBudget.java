package com.app.events.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "event_budgets")
public class EventBudget extends BaseEntity {
    private String eventId;
    private double totalBudget;
    private String currency;
    private List<BudgetCategory> categories = new ArrayList<>();
}
