package com.cicd;

import com.cicd.server.PipelineServer;

import java.awt.Desktop;
import java.net.URI;

public class App {
    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println(" 🚀 AUTOMATED CI/CD PIPELINE & DEPLOYMENT SYSTEM (JAVA 21) ");
        System.out.println("==================================================================");

        int port = 8081;
        PipelineServer server = new PipelineServer(port);

        try {
            server.start();
            System.out.println("✅ Pipeline Dashboard Server active at: http://localhost:" + port);
            System.out.println("👉 Opening http://localhost:" + port + " in your browser...");

            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI("http://localhost:" + port));
                } else {
                    Runtime.getRuntime().exec("cmd /c start http://localhost:" + port);
                }
            } catch (Exception ex) {
                Runtime.getRuntime().exec("cmd /c start http://localhost:" + port);
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to start CI/CD Server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
