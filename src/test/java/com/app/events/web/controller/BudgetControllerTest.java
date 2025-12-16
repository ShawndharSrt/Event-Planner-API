package com.app.events.web.controller;

import com.app.events.config.MongoConfig;
import com.app.events.dto.BudgetSummary;
import com.app.events.dto.CategorySpentSummary;
import com.app.events.dto.BudgetSummary;
import com.app.events.dto.BudgetUpdateRequest;
import com.app.events.dto.CategorySpentSummary;
import com.app.events.model.EventBudget;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

        private EventBudget eventBudget;
        private BudgetSummary budgetSummary;
        private Expense expense;

        @BeforeEach
        void setUp() {
                eventBudget = new EventBudget();
                eventBudget.setId("bdg-1");
                eventBudget.setEventId("evt-1");
                eventBudget.setTotalBudget(10000.0);
                eventBudget.setCurrency("USD");

                CategorySpentSummary categorySummary = new CategorySpentSummary(
                                "1",
                                "Venue",
                                5000.0,
                                1500.0,
                                "#FF5733",
                                "venue");

                budgetSummary = new BudgetSummary(
                                "bdg-1",
                                "evt-1",
                                10000.0,
                                1500.0,
                                "USD",
                                List.of(categorySummary));

                expense = new Expense();
                expense.setId("exp-1");
                expense.setAmount(100.0);
                expense.setDescription("Decor");
                expense.setEventId("evt-1");
                expense.setCategoryId("1");
                expense.setDate(new Date());
                expense.setStatus("paid");
        }

        @Test
        void getBudget_shouldReturnBudgetSummary() throws Exception {
                when(budgetService.getBudgetSummaryByEventId("evt-1")).thenReturn(budgetSummary);

                mockMvc.perform(get("/api/budget/evt-1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.id").value("bdg-1"))
                                .andExpect(jsonPath("$.data.totalBudget").value(10000.0))
                                .andExpect(jsonPath("$.data.totalSpent").value(1500.0));
        }

        @Test
        void updateBudget_shouldReturnUpdatedBudget() throws Exception {
                // Return existing eventBudget when upsert is called
                when(budgetService.upsertBudget(eq("evt-1"), any(BudgetUpdateRequest.class))).thenReturn(eventBudget);

                // Create update request DTO
                BudgetUpdateRequest updateRequest = new BudgetUpdateRequest();
                updateRequest.setTotalBudget(10000.0);
                updateRequest.setCurrency("USD");

                mockMvc.perform(patch("/api/budget/evt-1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.id").value("bdg-1"));
        }

        @Test
        void getExpenses_shouldReturnExpensesList() throws Exception {
                List<Expense> expenses = List.of(expense);
                when(budgetService.getExpensesByEventId("evt-1")).thenReturn(expenses);

                mockMvc.perform(get("/api/budget/evt-1/expenses"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data[0].id").value("exp-1"))
                                .andExpect(jsonPath("$.data[0].description").value("Decor"));
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

                mockMvc.perform(patch("/api/budget/expenses/exp-1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(expense)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.description").value("Updated Decor"));
        }

        @Test
        void deleteExpense_shouldReturnSuccess() throws Exception {
                doNothing().when(budgetService).deleteExpense("exp-1");

                mockMvc.perform(delete("/api/budget/expenses/exp-1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Expense deleted"));
        }
}
