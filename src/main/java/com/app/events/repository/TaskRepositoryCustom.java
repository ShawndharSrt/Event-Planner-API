package com.app.events.repository;

import com.app.events.model.Task;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepositoryCustom extends MongoRepository<Task, String> {

    List<Task> findByStatus(String status);
}
