package com.task.service;


import com.task.entity.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskExportService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public String exportTasksToS3(List<Task> tasks) {
        String csvContent = buildCsvContent(tasks);
        String fileName = "tasks_export_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType("text/csv")
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(csvContent.getBytes(StandardCharsets.UTF_8)));

        return fileName;
    }

    private String buildCsvContent(List<Task> tasks) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Title,Description,Status,DueDate\n");

        for (Task task : tasks) {
            sb.append(task.getId()).append(",")
                    .append(escapeCsv(task.getTitle())).append(",")
                    .append(escapeCsv(task.getDescription())).append(",")
                    .append(task.getStatus()).append(",")
                    .append(task.getDueDate() != null ? task.getDueDate().toString() : "").append("\n");
        }
        return sb.toString();
    }

    private String escapeCsv(String input) {
        if (input == null) return "";
        String escaped = input.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}