package com.app.events.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BudgetCategory extends BaseEntity {
    private String name;
    private double allocatedAmount;
    private String color;
    private String icon;
}
