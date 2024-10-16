package com.alexwang.blog.po;

import java.time.Instant;

public class RequestLog {
    private Instant timestamp; // 时间戳
    private String requestId;  // 请求 ID
    private String clientIp;   // 客户端 IP 地址
    private String userEmail;  // 用户邮箱
    private String httpMethod; // HTTP 请求方法 (GET, PUT, POST, etc.)
    private String requestPath; // 请求的 URI 路径
    private int httpStatusCode; // HTTP 响应状态码
    private long dataSize;      // 数据大小 (以字节为单位)
    private long responseTime;  // 响应时间 (以毫秒或微秒为单位)
    private long requestDuration; // 请求时长 (以毫秒为单位)
    private String clientAgent; // 客户端工具 (例如: jfrog-cli-go/2.56.1)

    // 构造函数
    public RequestLog(Instant timestamp, String requestId, String clientIp, String userEmail,
                      String httpMethod, String requestPath, int httpStatusCode,
                      long dataSize, long responseTime, long requestDuration, String clientAgent) {
        this.timestamp = timestamp;
        this.requestId = requestId;
        this.clientIp = clientIp;
        this.userEmail = userEmail;
        this.httpMethod = httpMethod;
        this.requestPath = requestPath;
        this.httpStatusCode = httpStatusCode;
        this.dataSize = dataSize;
        this.responseTime = responseTime;
        this.requestDuration = requestDuration;
        this.clientAgent = clientAgent;
    }

    // Getter 和 Setter 方法
    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public long getDataSize() {
        return dataSize;
    }

    public void setDataSize(long dataSize) {
        this.dataSize = dataSize;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public long getRequestDuration() {
        return requestDuration;
    }

    public void setRequestDuration(long requestDuration) {
        this.requestDuration = requestDuration;
    }

    public String getClientAgent() {
        return clientAgent;
    }

    public void setClientAgent(String clientAgent) {
        this.clientAgent = clientAgent;
    }

    @Override
    public String toString() {
        return "RequestLog{" +
                "timestamp=" + timestamp +
                ", requestId='" + requestId + '\'' +
                ", clientIp='" + clientIp + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", httpMethod='" + httpMethod + '\'' +
                ", requestPath='" + requestPath + '\'' +
                ", httpStatusCode=" + httpStatusCode +
                ", dataSize=" + dataSize +
                ", responseTime=" + responseTime +
                ", requestDuration=" + requestDuration +
                ", clientAgent='" + clientAgent + '\'' +
                '}';
    }
}
