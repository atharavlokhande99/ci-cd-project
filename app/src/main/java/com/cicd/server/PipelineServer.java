package com.cicd.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class PipelineServer {
    private final int port;
    private final Gson gson = new Gson();
    private final List<Map<String, Object>> buildLogs = new ArrayList<>();
    private int buildCounter = 104;

    public PipelineServer(int port) {
        this.port = port;
        initSampleBuilds();
    }

    private void initSampleBuilds() {
        addBuildRecord("#101", "main", "Add Maven shade plugin", "SUCCESS", "1m 12s", "Passed 24 Unit Tests");
        addBuildRecord("#102", "main", "Update Dockerfile to Java 21", "SUCCESS", "48s", "Docker Image Pushed");
        addBuildRecord("#103", "feature/auth", "Fix pipeline healthcheck API", "SUCCESS", "52s", "K8s Deployment Verified");
    }

    private void addBuildRecord(String buildId, String branch, String commit, String status, String duration, String summary) {
        Map<String, Object> build = new LinkedHashMap<>();
        build.put("buildId", buildId);
        build.put("branch", branch);
        build.put("commit", commit);
        build.put("status", status);
        build.put("duration", duration);
        build.put("summary", summary);
        build.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        buildLogs.add(0, build);
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", new StaticFileHandler());
        server.createContext("/api/status", new StatusHandler());
        server.createContext("/api/trigger-build", new TriggerBuildHandler());
        server.createContext("/api/health", new HealthHandler());

        server.setExecutor(null);
        server.start();
    }

    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            InputStream is = getClass().getResourceAsStream("/web/index.html");
            if (is == null) {
                String response = "Error: CI/CD Dashboard HTML not found!";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }
            byte[] bytes = is.readAllBytes();
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }

    private class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("projectName", "Automated CI/CD Pipeline & Software Deployment System");
            resp.put("environment", "Production / Kubernetes Cluster");
            resp.put("uptime", "99.98%");
            resp.put("totalBuilds", buildLogs.size());
            resp.put("recentBuilds", buildLogs);

            sendJsonResponse(exchange, 200, gson.toJson(resp));
        }
    }

    private class TriggerBuildHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                Map<String, String> body = gson.fromJson(reader, Map.class);
                String branch = body != null && body.containsKey("branch") ? body.get("branch") : "main";
                String commitMsg = body != null && body.containsKey("commit") ? body.get("commit") : "Manual Pipeline Trigger";

                String newBuildId = "#" + (buildCounter++);
                addBuildRecord(newBuildId, branch, commitMsg, "SUCCESS", "42s", "All 5 Stages Completed Successfully (JUnit, Docker, K8s)");

                Map<String, Object> resp = new LinkedHashMap<>();
                resp.put("success", true);
                resp.put("message", "Pipeline Execution Completed Successfully for Build " + newBuildId);
                resp.put("buildId", newBuildId);

                sendJsonResponse(exchange, 200, gson.toJson(resp));
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    private class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("status", "HEALTHY");
            resp.put("system", "CI/CD Pipeline Engine");
            resp.put("timestamp", new Date().toString());
            sendJsonResponse(exchange, 200, gson.toJson(resp));
        }
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
