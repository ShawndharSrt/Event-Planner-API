package com.app.events.web.controller;

import com.app.events.config.MongoConfig;
import com.app.events.dto.ApiResponse;
import com.app.events.model.Budget;
import com.app.events.model.BudgetCategory;
import com.app.events.model.Expense;
import com.app.events.service.BudgetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = BudgetController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
}, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MongoConfig.class))
class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BudgetService budgetService;

    @Autowired
    private ObjectMapper objectMapper;

    private Budget budget;
    private Expense expense;

    @BeforeEach
    void setUp() {
        budget = new Budget();
        budget.setId("bdg-1");
        budget.setEventId("evt-1");
        budget.setTotalBudget(10000.0);
        budget.setCurrency("USD");

        expense = new Expense();
        expense.setId("exp-1");
        expense.setAmount(100.0);
        expense.setDescription("Decor");
        expense.setEventId("evt-1");
    }

    @Test
    void getBudget_shouldReturnBudget() throws Exception {
        when(budgetService.getBudgetByEventId("evt-1")).thenReturn(Optional.of(budget));

        mockMvc.perform(get("/api/budget/evt-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("bdg-1"));
    }

    @Test
    void getBudget_shouldReturnNoBudgetFound_whenBudgetDoesNotExist() throws Exception {
        // Controller returns "success" with null data in this case
        when(budgetService.getBudgetByEventId("evt-999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/budget/evt-999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.message").value("No budget found"));
    }

    @Test
    void updateBudget_shouldReturnUpdatedBudget() throws Exception {
        when(budgetService.upsertBudget(eq("evt-1"), any(Budget.class))).thenReturn(budget);

        mockMvc.perform(put("/api/budget/evt-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(budget)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("bdg-1"));
    }

    @Test
    void addExpense_shouldReturnAddedExpense() throws Exception {
        when(budgetService.addExpense(any(Expense.class))).thenReturn(expense);

        mockMvc.perform(post("/api/budget/evt-1/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("exp-1"));
    }

    @Test
    void updateExpense_shouldReturnUpdatedExpense() throws Exception {
        expense.setDescription("Updated Decor");
        when(budgetService.updateExpense(eq("exp-1"), any(Expense.class))).thenReturn(expense);

        mockMvc.perform(patch("/api/budget/exp-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.description").value("Updated Decor"));
    }

    @Test
    void deleteExpense_shouldReturnSuccess() throws Exception {
        doNothing().when(budgetService).deleteExpense("exp-1");

        mockMvc.perform(delete("/api/budget/exp-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Expense deleted"));
    }

    @Test
    void getCategoryById_shouldReturnCategory() throws Exception {
        BudgetCategory category = new BudgetCategory();
        category.setId("cat-1");
        category.setName("Venue");

        when(budgetService.getCategoryById("cat-1")).thenReturn(Optional.of(category));

        mockMvc.perform(get("/api/budget/budget-categories/cat-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Venue"));
    }
}
