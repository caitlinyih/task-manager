package com.github.caitlinyih.taskmanager.it;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.caitlinyih.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration tests that verify behaviour requiring a real database.
 * Complements the unit tests in {@code controller/} and {@code service/}
 * by covering full CRUD persistence, entity mapping, and error handling
 * paths (malformed JSON, invalid enums) that only surface with a live
 * Spring context and PostgreSQL.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class TaskIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    @Test
    void fullTaskLifecycle_createReadUpdateDelete() throws Exception {
        // Create
        String body = """
            {
                "title": "Review documents",
                "description": "Check all files",
                "status": "TODO",
                "dueDateTime": "2026-06-01T10:00:00"
            }
            """;

        MvcResult createResult = mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.title", is("Review documents")))
            .andExpect(jsonPath("$.status", is("TODO")))
            .andExpect(jsonPath("$.dueDateTime", is("2026-06-01T10:00:00")))
            .andReturn();

        long id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // Read
        mockMvc.perform(get("/tasks/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title", is("Review documents")))
            .andExpect(jsonPath("$.description", is("Check all files")));

        // Update status
        mockMvc.perform(patch("/tasks/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"status": "COMPLETED"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status", is("COMPLETED")));

        // Verify update persisted
        mockMvc.perform(get("/tasks/{id}", id))
            .andExpect(jsonPath("$.status", is("COMPLETED")));

        // Delete
        mockMvc.perform(delete("/tasks/{id}", id))
            .andExpect(status().isNoContent());

        // Verify deleted
        mockMvc.perform(get("/tasks/{id}", id))
            .andExpect(status().isNotFound());
    }

    @Test
    void givenNoTasks_whenGetTasks_thenReturnsEmptyList() throws Exception {
        mockMvc.perform(get("/tasks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void givenMalformedJson_whenPostTasks_thenReturns400() throws Exception {
        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("not json"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", is("Invalid request body")));
    }

    @Test
    void givenInvalidStatus_whenPostTasks_thenReturns400() throws Exception {
        String body = """
            {
                "title": "A task",
                "status": "INVALID",
                "dueDateTime": "2026-06-01T10:00:00"
            }
            """;

        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());
    }
}
