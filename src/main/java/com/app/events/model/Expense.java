package com.app.events.model;

import lombok.Data;

@Data
public class Expense {
    private String id;
    private String eventId;
    private String categoryId;
    private String categoryName;
    private String description;
    private String vendor;
    private double amount;
    private String date;
    private String status; // pending, paid, overdue
    private String notes;
}
