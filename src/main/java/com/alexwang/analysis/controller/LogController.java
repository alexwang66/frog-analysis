package com.alexwang.analysis.controller;

import com.alexwang.analysis.po.RequestLog;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Controller
public class LogController {

    // Handle the upload of the log file and redirect to the speed analysis page
//    @PostMapping("/upload-log")
//    public String uploadLogFile(@RequestParam("logFile") MultipartFile logFile, Model model) {
//        // Parse the uploaded log file
//        List<RequestLog> logs = parseLogFile(logFile);  // Correctly parse the file here
//
//        // Pass the parsed logs to the speed page for analysis
//        model.addAttribute("logs", logs);
//
//        // Redirect to the speed analysis page
//        return "redirect:/speed";
//    }

    // Speed analysis page, now receiving the parsed logs from the model
    @GetMapping("/requestLog")
    public String getRequestLog(Model model) {
        // Get the parsed logs from the model
        List<RequestLog> logs = (List<RequestLog>) model.asMap().get("logs");

        // Ensure logs are not null
        if (logs == null || logs.isEmpty()) {
            return "error";  // Return an error page if logs are missing
        }

        // Perform the speed analysis
        List<String> timeLabels = new ArrayList<>();
        Map<Long, Long> totalDownloadSizePerSecond = new TreeMap<>();
        Map<Long, Double> totalDurationPerSecond = new TreeMap<>();
        Map<Long, Integer> concurrentConnectionsMap = new TreeMap<>();

        // Iterate over logs to perform the analysis
        for (RequestLog log : logs) {
            if (log.getDataSize() < 0) continue;

            long timestampInSeconds = log.getTimestamp().toEpochMilli() / 1000;

            concurrentConnectionsMap.put(timestampInSeconds, concurrentConnectionsMap.getOrDefault(timestampInSeconds, 0) + 1);
            totalDownloadSizePerSecond.put(timestampInSeconds, totalDownloadSizePerSecond.getOrDefault(timestampInSeconds, 0L) + log.getDataSize());
            totalDurationPerSecond.put(timestampInSeconds, totalDurationPerSecond.getOrDefault(timestampInSeconds, 0.0) + log.getRequestDuration() / 1000.0);
        }

        List<Double> downloadSpeeds = new ArrayList<>();
        List<Double> requestedVolumes = new ArrayList<>();

        for (Long timestamp : totalDownloadSizePerSecond.keySet()) {
            long totalDownloadSize = totalDownloadSizePerSecond.get(timestamp);
            double totalDuration = totalDurationPerSecond.get(timestamp);

            if (totalDuration == 0) {
                downloadSpeeds.add(0.0);
            } else {
                double speedInMBPerSecond = (totalDownloadSize / 1024.0 / 1024.0) / totalDuration;
                speedInMBPerSecond = Math.round(speedInMBPerSecond * 1000.0) / 1000.0;
                downloadSpeeds.add(speedInMBPerSecond);
            }

            double requestedVolumeInMB = totalDownloadSize / 1024.0 / 1024.0;
            requestedVolumeInMB = Math.round(requestedVolumeInMB * 1000.0) / 1000.0;
            requestedVolumes.add(requestedVolumeInMB);

            timeLabels.add(Instant.ofEpochSecond(timestamp).toString());
        }

        List<Integer> concurrentConnections = new ArrayList<>(concurrentConnectionsMap.values());

        String timeLabelsForJs = "[" + timeLabels.stream()
                .map(label -> "\"" + label + "\"")
                .collect(Collectors.joining(", ")) + "]";

        model.addAttribute("timeLabelsForJs", timeLabelsForJs);
        model.addAttribute("downloadSpeeds", downloadSpeeds);
        model.addAttribute("requestedVolumes", requestedVolumes);
        model.addAttribute("concurrentConnections", concurrentConnections);

        return "requestLog";
    }

    // Method to parse the uploaded log file
    private List<RequestLog> parseLogFile(MultipartFile logFile) {
        List<RequestLog> logs = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(logFile.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logs.add(parseLog(line));  // Parse each line into a RequestLog object
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return logs;
    }

    // Helper method to parse individual log lines
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

        return new RequestLog(timestamp, requestId, clientIp, userEmail, httpMethod, requestPath, httpStatusCode, dataSize, responseTime, requestDuration, clientAgent);
    }
}
