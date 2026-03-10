package com.github.caitlinyih.taskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.github.caitlinyih.taskmanager.model.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
