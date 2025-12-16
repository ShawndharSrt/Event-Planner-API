package com.app.events.service.scheduler;

import com.app.events.model.EventBudget;
import com.app.events.model.Expense;
import com.app.events.model.Task;
import com.app.events.model.enums.AlertCode;
import com.app.events.repository.EventBudgetRepository;
import com.app.events.repository.ExpenseRepository;
import com.app.events.repository.NotificationRepository;
import com.app.events.repository.TaskRepository;
import com.app.events.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AlertSchedulerTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private EventBudgetRepository eventBudgetRepository;
    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AlertScheduler alertScheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void checkTaskDueDates_ShouldCreateAlert_WhenTasksDueAndNoAlertExists() {
        Task task = new Task();
        task.setEventId("event1");
        task.setTitle("Test Task");

        when(taskRepository.findByDueDateBetweenAndStatusNot(any(), any(), anyString()))
                .thenReturn(List.of(task));
        when(notificationRepository.existsByEventIdAndCodeAndReadFalse("event1", AlertCode.TDDA.getCode()))
                .thenReturn(false);

        alertScheduler.runOfflineAlertChecks();

        verify(notificationService).createAlert(eq(AlertCode.TDDA), isNull(), eq("event1"), anyString());
    }

    @Test
    void checkBudgetExceeded_ShouldCreateAlert_WhenBudgetExceeded() {
        EventBudget budget = new EventBudget();
        budget.setEventId("event1");
        budget.setTotalBudget(1000.0);

        Expense expense = new Expense();
        expense.setAmount(850.0); // 85%

        when(eventBudgetRepository.findAll()).thenReturn(List.of(budget));
        when(expenseRepository.findByEventId("event1")).thenReturn(List.of(expense));
        when(notificationRepository.existsByEventIdAndCodeAndReadFalse("event1", AlertCode.B8EA.getCode()))
                .thenReturn(false);

        alertScheduler.runOfflineAlertChecks();

        verify(notificationService).createAlert(eq(AlertCode.B8EA), isNull(), eq("event1"), anyString());
    }
}
