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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertScheduler {

    private final TaskRepository taskRepository;
    private final EventBudgetRepository eventBudgetRepository;
    private final ExpenseRepository expenseRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    // Run every hour
    // @Scheduled(cron = "0 4 13 * * *") // Runs at 13:02:00 every day
    public void runOfflineAlertChecks() {
        log.info("Running offline alert checks...");
        checkTaskDueDates();
        checkTaskOverdue();
        checkBudgetExceeded();
        checkPendingPayments();
    }

    // TDDA - Task Due Date Alert (Due Tomorrow)
    private void checkTaskDueDates() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        // Find tasks due on 'tomorrow' (ignoring time component of finding, using
        // strict date match logic or range)
        // Since findAll is efficient enough for this context or use custom repo method
        // Using stream for grouping ease
        List<Task> tasks = taskRepository.findByDueDateBetweenAndStatusNot(tomorrow, tomorrow, "completed");

        // Group by Event ID
        Map<String, List<Task>> tasksByEvent = tasks.stream()
                .filter(t -> t.getEventId() != null)
                .collect(Collectors.groupingBy(Task::getEventId));

        tasksByEvent.forEach((eventId, eventTasks) -> {
            if (!notificationRepository.existsByEventIdAndCodeAndReadFalse(eventId, AlertCode.TDDA.getCode())) {
                notificationService.createAlert(AlertCode.TDDA, null, eventId,
                        "(" + eventTasks.size() + " tasks due tomorrow)");
            }
        });
    }

    // TOA - Task Overdue Alert
    private void checkTaskOverdue() {
        LocalDate today = LocalDate.now();
        List<Task> tasks = taskRepository.findByDueDateBeforeAndStatusNot(today, "completed");

        Map<String, List<Task>> tasksByEvent = tasks.stream()
                .filter(t -> t.getEventId() != null)
                .collect(Collectors.groupingBy(Task::getEventId));

        tasksByEvent.forEach((eventId, eventTasks) -> {
            if (!notificationRepository.existsByEventIdAndCodeAndReadFalse(eventId, AlertCode.TOA.getCode())) {
                notificationService.createAlert(AlertCode.TOA, null, eventId,
                        "(" + eventTasks.size() + " tasks overdue)");
            }
        });
    }

    // B8EA - Budget 80% Exceed Alert
    private void checkBudgetExceeded() {
        List<EventBudget> budgets = eventBudgetRepository.findAll();
        for (EventBudget budget : budgets) {
            String eventId = budget.getEventId();
            if (eventId == null)
                continue;

            List<Expense> expenses = expenseRepository.findByEventId(eventId);
            double totalSpent = expenses.stream().mapToDouble(Expense::getAmount).sum();

            if (budget.getTotalBudget() > 0 && totalSpent >= 0.8 * budget.getTotalBudget()) {
                if (!notificationRepository.existsByEventIdAndCodeAndReadFalse(eventId, AlertCode.B8EA.getCode())) {
                    notificationService.createAlert(AlertCode.B8EA, null, eventId,
                            String.format("Spent: %.2f / %.2f", totalSpent, budget.getTotalBudget()));
                }
            }
        }
    }

    // EPDA - Expenses Payment Due Alert
    private void checkPendingPayments() {
        List<Expense> pendingExpenses = expenseRepository.findByStatus("pending");
        LocalDate today = LocalDate.now();

        // Assuming expense 'date' is the intended payment date. Alert if pending and
        // date is passed or today.
        Map<String, List<Expense>> expensesByEvent = pendingExpenses.stream()
                .filter(e -> e.getDate() != null &&
                        toLocalDate(e.getDate()).isBefore(today.plusDays(1)) &&
                        e.getEventId() != null)
                .collect(Collectors.groupingBy(Expense::getEventId));

        expensesByEvent.forEach((eventId, expenses) -> {
            if (!notificationRepository.existsByEventIdAndCodeAndReadFalse(eventId, AlertCode.EPDA.getCode())) {
                notificationService.createAlert(AlertCode.EPDA, null, eventId,
                        "(" + expenses.size() + " payments due)");
            }
        });
    }

    private LocalDate toLocalDate(java.util.Date date) {
        return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }
}
