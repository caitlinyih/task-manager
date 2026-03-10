package com.github.caitlinyih.taskmanager.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.caitlinyih.taskmanager.dto.CreateTaskRequest;
import com.github.caitlinyih.taskmanager.dto.UpdateStatusRequest;
import com.github.caitlinyih.taskmanager.model.Task;
import com.github.caitlinyih.taskmanager.model.TaskStatus;
import com.github.caitlinyih.taskmanager.service.TaskService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Nested
    @DisplayName("POST /tasks")
    class CreateTask {

        @Test
        @DisplayName("given a valid request, when POST /tasks is called, then it returns 201 with the created task")
        void givenValidRequest_whenPostTasks_thenReturns201() throws Exception {
            CreateTaskRequest request = new CreateTaskRequest(
                "New Task", "Description", TaskStatus.TODO, LocalDateTime.of(2026, 6, 1, 10, 0)
            );
            Task created = new Task(1L, "New Task", "Description", TaskStatus.TODO, request.getDueDateTime());
            when(taskService.createTask(any())).thenReturn(created);

            mockMvc.perform(post("/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("New Task")));
        }

        @Test
        @DisplayName("given the title is missing, when POST /tasks is called, then it returns 400")
        void givenTitleMissing_whenPostTasks_thenReturns400() throws Exception {
            CreateTaskRequest request = new CreateTaskRequest(
                "", null, TaskStatus.TODO, LocalDateTime.of(2026, 6, 1, 10, 0)
            );

            mockMvc.perform(post("/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /tasks")
    class GetAllTasks {

        @Test
        @DisplayName("given tasks exist, when GET /tasks is called, then it returns the task list")
        void givenTasksExist_whenGetTasks_thenReturnsTaskList() throws Exception {
            Task task = new Task(1L, "Task", null, TaskStatus.TODO, LocalDateTime.now());
            when(taskService.getAllTasks()).thenReturn(List.of(task));

            mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Task")));
        }
    }

    @Nested
    @DisplayName("GET /tasks/{id}")
    class GetTaskById {

        @Test
        @DisplayName("given the task exists, when GET /tasks/{id} is called, then it returns 200 with the task")
        void givenTaskExists_whenGetTaskById_thenReturns200() throws Exception {
            Task task = new Task(1L, "Task", null, TaskStatus.TODO, LocalDateTime.now());
            when(taskService.getTaskById(1L)).thenReturn(Optional.of(task));

            mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Task")));
        }

        @Test
        @DisplayName("given the task does not exist, when GET /tasks/{id} is called, then it returns 404")
        void givenTaskDoesNotExist_whenGetTaskById_thenReturns404() throws Exception {
            when(taskService.getTaskById(1L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /tasks/{id}/status")
    class UpdateTaskStatus {

        @Test
        @DisplayName("given the task exists, when PATCH /tasks/{id}/status is called, then it returns 200 with the updated task")
        void givenTaskExists_whenPatchStatus_thenReturns200() throws Exception {
            UpdateStatusRequest request = new UpdateStatusRequest(TaskStatus.COMPLETED);
            Task updated = new Task(1L, "Task", null, TaskStatus.COMPLETED, LocalDateTime.now());
            when(taskService.updateTaskStatus(1L, TaskStatus.COMPLETED)).thenReturn(Optional.of(updated));

            mockMvc.perform(patch("/tasks/1/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")));
        }

        @Test
        @DisplayName("given the task does not exist, when PATCH /tasks/{id}/status is called, then it returns 404")
        void givenTaskDoesNotExist_whenPatchStatus_thenReturns404() throws Exception {
            UpdateStatusRequest request = new UpdateStatusRequest(TaskStatus.COMPLETED);
            when(taskService.updateTaskStatus(1L, TaskStatus.COMPLETED)).thenReturn(Optional.empty());

            mockMvc.perform(patch("/tasks/1/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /tasks/{id}")
    class DeleteTask {

        @Test
        @DisplayName("given the task exists, when DELETE /tasks/{id} is called, then it returns 204")
        void givenTaskExists_whenDeleteTask_thenReturns204() throws Exception {
            when(taskService.deleteTask(1L)).thenReturn(true);

            mockMvc.perform(delete("/tasks/1"))
                .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("given the task does not exist, when DELETE /tasks/{id} is called, then it returns 404")
        void givenTaskDoesNotExist_whenDeleteTask_thenReturns404() throws Exception {
            when(taskService.deleteTask(1L)).thenReturn(false);

            mockMvc.perform(delete("/tasks/1"))
                .andExpect(status().isNotFound());
        }
    }
}
