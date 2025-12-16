package com.app.events.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategorySpentSummary {
    private String categoryId;
    private String categoryName;
    private double allocatedAmount;
    private double spentAmount;
    private String color;
    private String icon;
}
