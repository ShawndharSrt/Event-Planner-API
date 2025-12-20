package com.app.events.repository;

import com.app.events.model.Task;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends MongoRepository<Task, String> {
    List<Task> findByEventId(String eventId);

    List<Task> findByStatusNot(String status);

    List<Task> findByDueDateBeforeAndStatusNot(LocalDate date, String status);

    List<Task> findByDueDateBetweenAndStatusNot(LocalDate start, LocalDate end, String status);

    long countByCreatedByAndStatus(String userId, String status);

    List<Task> findByCreatedByAndDueDateBetween(String createdBy, LocalDate start, LocalDate end);

    List<Task> findByCreatedByAndEventIdAndDueDateBetween(String createdBy, String eventId, LocalDate start,
            LocalDate end);
}
