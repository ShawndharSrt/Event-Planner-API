package com.app.events.service.impl;

import com.app.events.model.Budget;
import com.app.events.repository.BudgetRepository;
import com.app.events.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {
    private final BudgetRepository budgetRepository;

    @Override
    public Optional<Budget> getBudgetByEventId(String eventId) {
        return budgetRepository.findByEventId(eventId);
    }

    @Override
    public Budget upsertBudget(String eventId, Budget budget) {
        budget.setEventId(eventId);
        return budgetRepository.save(budget);
    }

    @Override
    public List<Budget> getAllBudgetsForEvent(String eventId) {
        return budgetRepository.findAllByEventId(eventId);
    }
}

