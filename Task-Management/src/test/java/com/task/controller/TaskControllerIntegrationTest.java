package com.task.controller;


import com.task.model.TaskRequest;
import com.task.model.TaskStatus;
import com.task.service.TaskExportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskExportService taskExportService;

    @Test
    void shouldPerformCrudAndExportFlow() throws Exception {
        TaskRequest createReq = new TaskRequest("Integration Task", "Build full pipeline", TaskStatus.TODO, LocalDate.of(2026, 12, 31));

        // 1. Create Task
        String responseContent = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title", is("Integration Task")))
                .andReturn().getResponse().getContentAsString();

        Long taskId = objectMapper.readTree(responseContent).get("id").asLong();

        // 2. Read Task
        mockMvc.perform(get("/tasks/" + taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Integration Task")));

        // 3. Update Task
        TaskRequest updateReq = new TaskRequest("Updated Task", "Updated Description", TaskStatus.IN_PROGRESS, LocalDate.of(2026, 12, 31));
        mockMvc.perform(put("/tasks/" + taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Task")))
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));

        // 4. Export Tasks to S3
        when(taskExportService.exportTasksToS3(anyList())).thenReturn("tasks_export_2026.csv");
        mockMvc.perform(post("/tasks/export"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName", is("tasks_export_2026.csv")));

        // 5. Delete Task
        mockMvc.perform(delete("/tasks/" + taskId))
                .andExpect(status().isNoContent());

        // 6. Verify Deletion
        mockMvc.perform(get("/tasks/" + taskId))
                .andExpect(status().isNotFound());
    }
}