package com.app.events.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "expenses")
public class Expense extends BaseEntity {
    @Indexed
    private String eventId;

    @Indexed
    private String categoryId;

    private String description;
    private String vendor;
    private double amount;

    @Indexed
    private Date date;

    private String status; // paid, pending
    private String notes;
}
