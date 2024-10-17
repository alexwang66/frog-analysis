package com.alexwang.analysis.controller;
import com.alexwang.analysis.po.RequestLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Controller
public class LogController {

    @Autowired
    private ResourceLoader resourceLoader;

    // 解析日志文件并返回 RequestLog 列表
    @GetMapping("/logs")
    public String getLogs(Model model) {
        List<RequestLog> logs = parseLogFile();

        // 按时间聚合下载量
        List<String> timeLabels = logs.stream()
                .filter(log -> log.getDataSize() >= 0)  // 过滤掉下载量为负数的日志条目
                .map(log -> log.getTimestamp().toString())  // 你可以根据需要格式化日期
                .collect(Collectors.toList());

        // 将时间标签处理为合法的 JavaScript 字符串数组格式
        String timeLabelsForJs = "[" + timeLabels.stream()
                .map(label -> "\"" + label + "\"")
                .collect(Collectors.joining(", ")) + "]";

        // 模拟下载量
        List<Long> downloadVolumes = logs.stream()
                .map(RequestLog::getDataSize)
                .filter(dataSize -> dataSize >= 0)  // 过滤掉负数的数据
                .collect(Collectors.toList());

        model.addAttribute("timeLabelsForJs", timeLabelsForJs);  // 传递处理后的字符串数组
        model.addAttribute("downloadVolumes", downloadVolumes);

        return "logs"; // 返回日志视图 (logs.html)
    }


    // 使用 ResourceLoader 读取类路径中的日志文件
    private List<RequestLog> parseLogFile() {
        List<RequestLog> logs = new ArrayList<>();

        try {
            // 使用 ResourceLoader 来加载类路径下的资源
            Resource resource = resourceLoader.getResource("classpath:data/artifactory-request.log");

            // 使用 BufferedReader 读取文件内容
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                logs.add(parseLog(line));  // 解析每一行日志
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return logs;
    }

    // 解析日志行
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
