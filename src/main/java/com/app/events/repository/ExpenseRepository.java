package com.app.events.repository;

import com.app.events.model.Expense;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ExpenseRepository extends MongoRepository<Expense, String> {
    List<Expense> findByEventId(String eventId);

    List<Expense> findByCategoryId(String categoryId);

    List<Expense> findByEventIdAndCategoryId(String eventId, ObjectId categoryId);

    void deleteByEventId(String eventId);

    List<Expense> findByStatus(String status);
}
