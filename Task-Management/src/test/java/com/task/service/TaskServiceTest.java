package com.task.service;


import com.task.entity.Task;
import com.task.exception.ResourceNotFoundException;
import com.task.model.TaskRequest;
import com.task.model.TaskResponse;
import com.task.model.TaskStatus;
import com.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskExportService taskExportService;

    @InjectMocks
    private TaskService taskService;

    private Task sampleTask;

    @BeforeEach
    void setUp() {
        sampleTask = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.TODO)
                .dueDate(LocalDate.now().plusDays(1))
                .build();
    }

    @Test
    void createTask_ShouldReturnCreatedTaskResponse() {
        TaskRequest request = new TaskRequest("Test Task", "Test Description", TaskStatus.TODO, LocalDate.now().plusDays(1));
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        TaskResponse response = taskService.createTask(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Test Task");
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void getTaskById_WhenExists_ShouldReturnTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));

        TaskResponse response = taskService.getTaskById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Test Task");
    }

    @Test
    void getTaskById_WhenNotFound_ShouldThrowException() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found with ID: 99");
    }

    @Test
    void exportTasks_ShouldDelegateToExportService() {
        when(taskRepository.findAll()).thenReturn(List.of(sampleTask));
        when(taskExportService.exportTasksToS3(anyList())).thenReturn("tasks_export_test.csv");

        String fileName = taskService.exportTasks();

        assertThat(fileName).isEqualTo("tasks_export_test.csv");
        verify(taskExportService, times(1)).exportTasksToS3(anyList());
    }
}