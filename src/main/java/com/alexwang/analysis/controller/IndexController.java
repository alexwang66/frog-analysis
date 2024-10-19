package com.alexwang.analysis.controller;

import com.alexwang.analysis.po.RequestLog;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Controller
public class IndexController {

    // Display index page (the file upload page)
    @GetMapping("/")
    public String showIndex() {
        return "index";  // Serve index.html page
    }

    // Handle file upload and redirect to the speed page
    @PostMapping("/upload-log")
    public String uploadLogFile(@RequestParam("logFile") MultipartFile logFile, RedirectAttributes redirectAttributes) {
        // Process the uploaded log file
        List<RequestLog> logs = processLogFile(logFile);

        // Add the processed logs to RedirectAttributes so they are available in the /speed page
        redirectAttributes.addFlashAttribute("logs", logs);

        // Redirect to the speed page where logs will be analyzed and displayed
        return "redirect:/speed";
    }


    // Method to process the uploaded log file
    private List<RequestLog> processLogFile(MultipartFile logFile) {
        List<RequestLog> logs = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(logFile.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logs.add(parseLog(line));  // Parse each log line and add to the list
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return logs;  // Return the processed logs
    }

    // Method to parse each line of the log
    private RequestLog parseLog(String logLine) {
        String[] parts = logLine.split("\\|");

        if (parts.length != 11) {
            throw new IllegalArgumentException("Invalid log format");
        }

        Instant timestamp = Instant.parse(parts[0]);
        String requestId = parts[1];
        String clientIp = parts[2];
        String userEmail = parts[3];
        String httpMethod = parts[4];
        String requestPath = parts[5];
        int httpStatusCode = Integer.parseInt(parts[6]);
        long dataSize = Long.parseLong(parts[7]);
        long responseTime = Long.parseLong(parts[8]);
        long requestDuration = Long.parseLong(parts[9]);
        String clientAgent = parts[10];

        return new RequestLog(timestamp, requestId, clientIp, userEmail, httpMethod, requestPath, httpStatusCode,
                dataSize, responseTime, requestDuration, clientAgent);
    }

    // Method to process the logs and perform analysis
    private void processLogs(List<RequestLog> logs, Model model) {
        long totalDataSize = 0;
        long totalDuration = 0;

        for (RequestLog log : logs) {
            totalDataSize += log.getDataSize();
            totalDuration += log.getRequestDuration();
        }

        long averageDuration = logs.size() > 0 ? totalDuration / logs.size() : 0;

        // Pass the results to the model to be used in the speed page
        model.addAttribute("totalDataSize", totalDataSize);
        model.addAttribute("averageDuration", averageDuration);
    }
}
