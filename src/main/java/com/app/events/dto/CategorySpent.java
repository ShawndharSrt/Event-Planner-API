package com.app.events.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for MongoDB aggregation results when calculating spent amounts per
 * category
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategorySpent {
    private String categoryId;
    private double totalSpent;
}
