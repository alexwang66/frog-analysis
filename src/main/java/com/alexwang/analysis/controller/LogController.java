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

    @GetMapping("/speed")
    public String getSpeed(Model model) {
        List<RequestLog> logs = parseLogFile();  // 解析日志文件

        // 初始化时间标签和每秒下载速度列表
        List<String> timeLabels = new ArrayList<>();
        List<Long> downloadSpeeds = new ArrayList<>();  // 使用 Long 类型

        // 定义字节到 MB 的转换系数
        long bytesToMB = 1024 * 1024;

        // 遍历日志，计算每秒下载量
        for (int i = 1; i < logs.size(); i++) {
            RequestLog previousLog = logs.get(i - 1);
            RequestLog currentLog = logs.get(i);

            // 使用 toEpochMilli() 获取时间戳，计算时间差（秒）
            long timeDifferenceInSeconds = (currentLog.getTimestamp().toEpochMilli() - previousLog.getTimestamp().toEpochMilli()) / 1000;
            if (timeDifferenceInSeconds == 0) {
                continue;  // 跳过时间间隔为0的情况
            }

            // 计算下载量差（字节），然后计算每秒下载量并转换为 MB/s
            long dataDifference = currentLog.getDataSize() - previousLog.getDataSize();
            long speedInMBPerSecond = (dataDifference / bytesToMB) / timeDifferenceInSeconds;

            // 添加时间标签和下载速度
            timeLabels.add(currentLog.getTimestamp().toString());  // 你可以根据需要格式化日期
            downloadSpeeds.add(speedInMBPerSecond);  // 单位为 MB/s
        }

        // 将时间标签处理为合法的 JavaScript 字符串数组格式
        String timeLabelsForJs = "[" + timeLabels.stream()
                .map(label -> "\"" + label + "\"")
                .collect(Collectors.joining(", ")) + "]";

        // 将每秒下载速度传递给视图
        model.addAttribute("timeLabelsForJs", timeLabelsForJs);
        model.addAttribute("downloadSpeeds", downloadSpeeds);  // MB/s 数据传递到前端

        return "speed"; // 返回显示速度的视图 (speed.html)
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
