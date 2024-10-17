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
import java.util.Map;
import java.util.TreeMap;
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

    @GetMapping("/speed")
    public String getSpeed(Model model) {
        List<RequestLog> logs = parseLogFile();  // 解析日志文件

        // 初始化时间标签、每秒下载速度、请求量和每秒并发连接数列表
        List<String> timeLabels = new ArrayList<>();
        Map<Long, Long> totalDownloadSizePerSecond = new TreeMap<>();  // 每秒的总下载量（字节）
        Map<Long, Double> totalDurationPerSecond = new TreeMap<>();  // 每秒的总请求持续时间
        Map<Long, Integer> concurrentConnectionsMap = new TreeMap<>();  // 用于统计每秒的并发连接数

        // 遍历日志，计算每秒的下载量、请求持续时间，并统计并发连接数
        for (RequestLog log : logs) {
            // 过滤掉下载量为负数的请求
            if (log.getDataSize() < 0) {
                continue;
            }

            // 使用 toEpochMilli() 获取时间戳，按秒单位统计
            long timestampInSeconds = log.getTimestamp().toEpochMilli() / 1000;

            // 更新并发连接数
            concurrentConnectionsMap.put(timestampInSeconds, concurrentConnectionsMap.getOrDefault(timestampInSeconds, 0) + 1);

            // 更新每秒的总下载量和总持续时间
            totalDownloadSizePerSecond.put(timestampInSeconds, totalDownloadSizePerSecond.getOrDefault(timestampInSeconds, 0L) + log.getDataSize());
            totalDurationPerSecond.put(timestampInSeconds, totalDurationPerSecond.getOrDefault(timestampInSeconds, 0.0) + log.getRequestDuration() / 1000.0);  // 将毫秒转换为秒
        }

        // 计算每秒的下载速度（MB/s）和请求量（MB）
        List<Double> downloadSpeeds = new ArrayList<>();
        List<Double> requestedVolumes = new ArrayList<>();  // 以 MB 为单位的请求量列表

        for (Long timestamp : totalDownloadSizePerSecond.keySet()) {
            long totalDownloadSize = totalDownloadSizePerSecond.get(timestamp);  // 字节
            double totalDuration = totalDurationPerSecond.get(timestamp);  // 秒

            // 计算每秒下载速度 (MB/s)，并保留小数点后 3 位
            if (totalDuration == 0) {
                downloadSpeeds.add(0.0);
            } else {
                double speedInMBPerSecond = (totalDownloadSize / 1024.0 / 1024.0) / totalDuration;  // MB/s
                speedInMBPerSecond = Math.round(speedInMBPerSecond * 1000.0) / 1000.0;  // 保留小数点后三位
                downloadSpeeds.add(speedInMBPerSecond);
            }

            // 计算每秒的请求量 (MB)，并保留小数点后 3 位
            double requestedVolumeInMB = totalDownloadSize / 1024.0 / 1024.0;  // 将字节转换为 MB
            requestedVolumeInMB = Math.round(requestedVolumeInMB * 1000.0) / 1000.0;  // 保留小数点后三位
            requestedVolumes.add(requestedVolumeInMB);

            // 记录时间标签
            timeLabels.add(Instant.ofEpochSecond(timestamp).toString());
        }

        // 将并发连接数据转换为列表
        List<Integer> concurrentConnections = new ArrayList<>();
        for (Long time : concurrentConnectionsMap.keySet()) {
            concurrentConnections.add(concurrentConnectionsMap.get(time));
        }

        // 将时间标签处理为合法的 JavaScript 字符串数组格式
        String timeLabelsForJs = "[" + timeLabels.stream()
                .map(label -> "\"" + label + "\"")
                .collect(Collectors.joining(", ")) + "]";

        // 将每秒下载速度、请求量和并发连接数传递给视图
        model.addAttribute("timeLabelsForJs", timeLabelsForJs);
        model.addAttribute("downloadSpeeds", downloadSpeeds);  // MB/s 数据传递到前端
        model.addAttribute("requestedVolumes", requestedVolumes);  // MB 请求量传递到前端
        model.addAttribute("concurrentConnections", concurrentConnections);  // 并发连接数传递到前端

        return "speed"; // 返回显示速度、请求量和并发连接数的视图 (speed.html)
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
        long requestDuration = Long.parseLong(parts[9]);  // 持续时间应为毫秒

        String clientAgent = parts[10];

        return new RequestLog(timestamp, requestId, clientIp, userEmail, httpMethod, requestPath, httpStatusCode, dataSize, responseTime, requestDuration, clientAgent);
    }

}
