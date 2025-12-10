package com.app.events.model;

import lombok.Data;

@Data
public class BudgetCategory {
    private String id;
    private String name;
    private double allocatedAmount;
    private double spentAmount;
    private String color;
    private String icon;
}
