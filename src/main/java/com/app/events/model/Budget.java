package com.app.events.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "budget")
public class Budget extends BaseEntity {
    private String eventId;
    private double totalBudget;
    private double spent;
    private String notes;
}

