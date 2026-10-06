package com.task.model;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record TaskRequest(
        @NotBlank(message = "Title is mandatory")
        String title,

        String description,

        @NotNull(message = "Status is mandatory")
        TaskStatus status,

        LocalDate dueDate
) {}