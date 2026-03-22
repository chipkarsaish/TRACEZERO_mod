package com.example.hellofx.controllers;

import com.example.hellofx.MainController;
import com.example.hellofx.controllers.ReportsController.ReportSummary;
import com.example.hellofx.utils.HistoryManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    private MainController mainController;

    @FXML private Label lblFilesScanned;
    @FXML private Label lblFilesCleaned;
    @FXML private Label lblCriticalThreats;

    @FXML private Label lblRiskPct;
    @FXML private Label lblRiskDesc;
    @FXML private Label lblLastScan;
    @FXML private Arc arcFg;

    // 🚀 NEW: Dynamic Badge Elements
    @FXML private Label lblRiskBadge;
    @FXML private Circle circleRiskBadge;

    public void setMainController(MainController mc) {
        this.mainController = mc;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Run once on initial load
        Platform.runLater(this::loadDashboardStats);
        
        // 🚀 NEW: Register to listen for any new history updates (from anywhere in the app)
        HistoryManager.setDashboardUpdateListener(() -> Platform.runLater(this::loadDashboardStats));
    }

    public void loadDashboardStats() {
        List<ReportSummary> history = HistoryManager.loadHistory();

        // Safety check for fresh installs
        if (history == null) {
            history = new ArrayList<>();
        }

        int totalScanned = history.size();
        int totalCleaned = 0;
        int criticalThreats = 0;
        int totalScore = 0;

        for (ReportSummary report : history) {
            if (report.remMet() > 0 || report.score() > 0) {
                totalCleaned++;
            }
            if (report.score() >= 70 || (report.riskLevel() != null && report.riskLevel().contains("High"))) {
                criticalThreats++;
            }
            totalScore += report.score();
        }

        final int finalTotalScanned = totalScanned;
        final int finalTotalCleaned = totalCleaned;
        final int finalCriticalThreats = criticalThreats;
        final int finalTotalScore = totalScore;

        List<ReportSummary> finalHistory = history;
        Platform.runLater(() -> {
            lblFilesScanned.setText(String.valueOf(finalTotalScanned));
            lblFilesCleaned.setText(String.valueOf(finalTotalCleaned));
            lblCriticalThreats.setText(String.valueOf(finalCriticalThreats));

            if (finalTotalScanned > 0) {
                int avgScore = finalTotalScore / finalTotalScanned;
                lblRiskPct.setText(avgScore + "%");

                double gaugeLength = -300.0 * (avgScore / 100.0);
                arcFg.setLength(gaugeLength);

                String color = avgScore < 30 ? "#38c96e" : (avgScore < 70 ? "#ffb84d" : "#e84545");
                arcFg.setStroke(javafx.scene.paint.Color.web(color));

                // 🚀 NEW: Update the Badge dynamically!
                String badgeText = avgScore < 30 ? "SECURE" : (avgScore < 70 ? "WARNING" : "CRITICAL");
                lblRiskBadge.setText(badgeText);
                circleRiskBadge.setFill(javafx.scene.paint.Color.web(color));
                circleRiskBadge.setStyle("-fx-effect: dropshadow(gaussian, " + color + ", 5, 0.9, 0, 0);");

                ReportSummary lastReport = finalHistory.get(0);
                lblLastScan.setText("Last scan: " + lastReport.scanTime());

                if (finalCriticalThreats > 0) {
                    lblRiskDesc.setText("Found " + finalCriticalThreats + " critical threats\nacross " + finalTotalScanned + " files");
                } else if (finalTotalCleaned > 0) {
                    lblRiskDesc.setText("Metadata removed from\n" + finalTotalCleaned + " files");
                } else {
                    lblRiskDesc.setText("System is secure.\nNo threats detected.");
                }

            } else {
                lblRiskPct.setText("0%");
                arcFg.setLength(0);
                lblLastScan.setText("Last scan: Never");
                lblRiskDesc.setText("No files have been\nscanned yet");
                lblRiskBadge.setText("SECURE");
                circleRiskBadge.setFill(javafx.scene.paint.Color.web("#38c96e"));
                circleRiskBadge.setStyle("-fx-effect: dropshadow(gaussian, #38c96e, 5, 0.9, 0, 0);");
            }
        });
    }

    @FXML
    private void onQuickScan() {
        if (mainController != null) mainController.navigateToScanFiles();
    }

    @FXML
    private void onRunScan() {
        if (mainController != null) mainController.navigateToScanFiles();
    }

    @FXML
    private void onStartMonitoring() {
        if (mainController != null) mainController.navigateToFolderWatch();
    }
}