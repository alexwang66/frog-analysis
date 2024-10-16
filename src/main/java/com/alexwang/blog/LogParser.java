package com.alexwang.blog;
import com.alexwang.blog.aspect.logAspect;
import com.alexwang.blog.po.RequestLog;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class LogParser {

    // 解析日志行并返回 RequestLog 对象
    public static RequestLog parseLog(String logLine) {
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

    // 从日志文件中读取所有日志行
    public static List<RequestLog> parseLogFile(String filePath) {
        List<RequestLog> logs = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logs.add(parseLog(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return logs;
    }

    public static void main(String[] args) {
        String logFilePath = "data/artifactory-request.log"; // 日志文件路径

        // 解析日志文件
        List<RequestLog> logs = parseLogFile(logFilePath);

        // 打印每个 RequestLog 对象
        for (RequestLog log : logs) {
            System.out.println(log);
        }
    }
}
