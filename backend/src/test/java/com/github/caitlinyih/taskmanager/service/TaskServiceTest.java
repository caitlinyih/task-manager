package com.github.caitlinyih.taskmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.caitlinyih.taskmanager.dto.CreateTaskRequest;
import com.github.caitlinyih.taskmanager.model.Task;
import com.github.caitlinyih.taskmanager.model.TaskStatus;
import com.github.caitlinyih.taskmanager.repository.TaskRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Nested
    @DisplayName("createTask")
    class CreateTask {

        @Test
        @DisplayName("given a valid request, when createTask is called, then it saves and returns the task")
        void givenValidRequest_whenCreateTask_thenSavesAndReturnsTask() {
            CreateTaskRequest request = new CreateTaskRequest(
                "Test Task", "Description", TaskStatus.TODO, LocalDateTime.now().plusDays(1)
            );
            Task saved = new Task(1L, "Test Task", "Description", TaskStatus.TODO, request.getDueDateTime());
            when(taskRepository.save(any(Task.class))).thenReturn(saved);

            Task result = taskService.createTask(request);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("Test Task");
            verify(taskRepository).save(any(Task.class));
        }
    }

    @Nested
    @DisplayName("getAllTasks")
    class GetAllTasks {

        @Test
        @DisplayName("given tasks exist, when getAllTasks is called, then it returns all tasks")
        void givenTasksExist_whenGetAllTasks_thenReturnsAllTasks() {
            Task task = new Task(1L, "Task", null, TaskStatus.TODO, LocalDateTime.now());
            when(taskRepository.findAll()).thenReturn(List.of(task));

            List<Task> result = taskService.getAllTasks();

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getTaskById")
    class GetTaskById {

        @Test
        @DisplayName("given the task exists, when getTaskById is called, then it returns the task")
        void givenTaskExists_whenGetTaskById_thenReturnsTask() {
            Task task = new Task(1L, "Task", null, TaskStatus.TODO, LocalDateTime.now());
            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

            Optional<Task> result = taskService.getTaskById(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getTitle()).isEqualTo("Task");
        }

        @Test
        @DisplayName("given the task does not exist, when getTaskById is called, then it returns empty")
        void givenTaskDoesNotExist_whenGetTaskById_thenReturnsEmpty() {
            when(taskRepository.findById(1L)).thenReturn(Optional.empty());

            Optional<Task> result = taskService.getTaskById(1L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateTaskStatus")
    class UpdateTaskStatus {

        @Test
        @DisplayName("given the task exists, when updateTaskStatus is called, then it updates and returns the task")
        void givenTaskExists_whenUpdateTaskStatus_thenUpdatesAndReturnsTask() {
            Task task = new Task(1L, "Task", null, TaskStatus.TODO, LocalDateTime.now());
            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
            when(taskRepository.save(task)).thenReturn(task);

            Optional<Task> result = taskService.updateTaskStatus(1L, TaskStatus.COMPLETED);

            assertThat(result).isPresent();
            assertThat(result.get().getStatus()).isEqualTo(TaskStatus.COMPLETED);
        }

        @Test
        @DisplayName("given the task does not exist, when updateTaskStatus is called, then it returns empty")
        void givenTaskDoesNotExist_whenUpdateTaskStatus_thenReturnsEmpty() {
            when(taskRepository.findById(1L)).thenReturn(Optional.empty());

            Optional<Task> result = taskService.updateTaskStatus(1L, TaskStatus.COMPLETED);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteTask")
    class DeleteTask {

        @Test
        @DisplayName("given the task exists, when deleteTask is called, then it deletes and returns true")
        void givenTaskExists_whenDeleteTask_thenDeletesAndReturnsTrue() {
            Task task = new Task(1L, "Task", null, TaskStatus.TODO, LocalDateTime.now());
            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

            boolean result = taskService.deleteTask(1L);

            assertThat(result).isTrue();
            verify(taskRepository).delete(task);
        }

        @Test
        @DisplayName("given the task does not exist, when deleteTask is called, then it returns false")
        void givenTaskDoesNotExist_whenDeleteTask_thenReturnsFalse() {
            when(taskRepository.findById(1L)).thenReturn(Optional.empty());

            boolean result = taskService.deleteTask(1L);

            assertThat(result).isFalse();
            verify(taskRepository, never()).delete(any());
        }
    }
}
