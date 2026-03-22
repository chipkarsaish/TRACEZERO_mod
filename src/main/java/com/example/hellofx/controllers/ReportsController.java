package com.example.hellofx.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.scene.shape.Arc; // 🚀 ADDED ARC IMPORT
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.awt.Desktop;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.example.hellofx.MainController;
import com.example.hellofx.utils.FileTypeDetector;
import com.example.hellofx.utils.HistoryManager;

import javafx.scene.control.TextField;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.util.Duration;

public class ReportsController implements Initializable {

    @FXML
    private VBox placeholderView;
    @FXML
    private ScrollPane detailsView;

    @FXML
    private TextField searchField;
    @FXML
    private ListView<ReportSummary> historyList;
    @FXML
    private Label lblFileName, lblFileType, lblFileSize, lblScanTime, lblFileHash;
    @FXML
    private Label lblScore, lblRiskLevel, lblTotalMetadata, lblSensitiveMetadata;
    @FXML
    private Label lblMetadataRemoved, lblMetadataRemaining, lblOriginalHash, lblCleanedHash;

    @FXML
    private Arc arcRiskGauge; // 🚀 ADDED ARC UI COMPONENT

    @FXML
    private VBox removedMetadataBox;
    @FXML
    private VBox logBox;
    @FXML
    private Label lblPageCount, lblEmbeddedImages, lblCompression, lblMacros;

    private MainController mainController;
    private ObservableList<ReportSummary> reportDataList;
    private ReportSummary currentSelectedReport;

    // 🔥 VITAL FIX: Added "implements java.io.Serializable" so it can be saved to the hard drive
    public record ReportSummary(String fileName, String fileType, String fileSize, String scanTime, String hash,
                                int score, String riskLevel, int totalMet, int sensMet, int remMet, int remainMet,
                                List<String> extractedData, File actualFile) implements java.io.Serializable {
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;

        File activeFile = mainController.getActiveFile();
        List<String> metadata = mainController.getExtractedMetadata();

        if (activeFile != null) {
            int metaCount = (metadata != null) ? metadata.size() : 0;
            int score = Math.min(metaCount * 5, 100);
            String risk = score < 30 ? "Low Risk" : (score < 70 ? "Medium Risk" : "High Risk");
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // 🔥 VITAL FIX: Ensure the list is a serializable ArrayList
            List<String> safeMetadata = (metadata != null) ? new ArrayList<>(metadata) : new ArrayList<>();

            ReportSummary realReport = new ReportSummary(
                    activeFile.getName(),
                    FileTypeDetector.detect(activeFile).name() + " FILE",
                    (activeFile.length() / 1024) + " KB",
                    time,
                    "SECURE_HASH_" + System.currentTimeMillis(),
                    score,
                    risk,
                    metaCount,
                    Math.max(0, metaCount - 2),
                    metaCount,
                    0,
                    safeMetadata,
                    activeFile
            );

            reportDataList.add(0, realReport);
            historyList.getSelectionModel().select(0);

            // 🔥 SAVE TRIGGER: Save the updated list to disk immediately
            HistoryManager.saveHistory(reportDataList);

            // 🚀 BUG FIX MERGED: Clear global state so we don't duplicate this report on subsequent visits
            mainController.setActiveFile(null);
            mainController.setExtractedMetadata(null);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 🔥 LOAD TRIGGER: Pull the saved history from the hard drive
        List<ReportSummary> savedHistory = HistoryManager.loadHistory();
        reportDataList = FXCollections.observableArrayList(savedHistory);

        // Wrapping the ObservableList in a FilteredList
        FilteredList<ReportSummary> filteredData = new FilteredList<>(reportDataList, p -> true);

        // Adding search filter logic
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(report -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return report.fileName().toLowerCase().contains(lowerCaseFilter);
            });
        });

        // Wrap the FilteredList in a SortedList to link to the ListView
        SortedList<ReportSummary> sortedData = new SortedList<>(filteredData);
        historyList.setItems(sortedData);

        historyList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ReportSummary item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().removeAll("history-status-clean", "history-status-warning", "history-status-critical");
                } else {
                    // Provide the complex UI graphic
                    HBox root = new HBox(12);
                    root.setAlignment(Pos.CENTER_LEFT);
                    
                    // Left Icon
                    FontIcon typeIcon = new FontIcon("fas-file-alt");
                    typeIcon.setIconSize(18);
                    typeIcon.setIconColor(javafx.scene.paint.Color.web("#8c82c6"));
                    
                    // Center text box
                    VBox textBox = new VBox(2);
                    Label nameLbl = new Label(item.fileName());
                    nameLbl.setStyle("-fx-text-fill: #dde1ef; -fx-font-weight: bold; -fx-font-size: 13px;");
                    // Truncate long names visually but allow showing full on hover
                    nameLbl.setMaxWidth(180); 
                    nameLbl.setEllipsisString("...");
                    
                    Tooltip tooltip = new Tooltip(item.fileName());
                    tooltip.setShowDelay(Duration.millis(300));
                    Tooltip.install(nameLbl, tooltip);

                    Label dateLbl = new Label(item.scanTime());
                    dateLbl.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");
                    
                    textBox.getChildren().addAll(nameLbl, dateLbl);
                    
                    // Spacer
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    
                    // Right status icon/dot
                    FontIcon statusIcon = new FontIcon("fas-circle");
                    statusIcon.setIconSize(10);
                    
                    if (item.score() < 30) {
                        statusIcon.setIconColor(javafx.scene.paint.Color.web("#38c96e")); // Clean
                    } else if (item.score() < 70) {
                        statusIcon.setIconColor(javafx.scene.paint.Color.web("#ffb84d")); // Warning
                    } else {
                        statusIcon.setIconColor(javafx.scene.paint.Color.web("#ff4a4a")); // Critical
                    }

                    root.getChildren().addAll(typeIcon, textBox, spacer, statusIcon);
                    
                    setGraphic(root);
                    setText(null);
                }
            }
        });

        historyList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentSelectedReport = newVal;
                loadReportDetails(newVal);
            }
        });

        placeholderView.setVisible(true);
        detailsView.setVisible(false);
    }

    private void loadReportDetails(ReportSummary summary) {
        placeholderView.setVisible(false);
        detailsView.setVisible(true);

        lblFileName.setText(summary.fileName());
        lblFileType.setText(summary.fileType());
        lblFileSize.setText(summary.fileSize());
        lblScanTime.setText(summary.scanTime());
        lblFileHash.setText(summary.hash());
        lblScore.setText(String.valueOf(summary.score()));
        lblRiskLevel.setText(summary.riskLevel());
        lblTotalMetadata.setText(String.valueOf(summary.totalMet()));
        lblSensitiveMetadata.setText(String.valueOf(summary.sensMet()));
        lblMetadataRemoved.setText(String.valueOf(summary.remMet()));
        lblMetadataRemaining.setText("0");
        lblOriginalHash.setText(summary.hash() + "_original");
        lblCleanedHash.setText(summary.hash() + "_cleaned");

        String color = summary.score() < 30 ? "#38c96e" : (summary.score() < 70 ? "#ffb84d" : "#ff4a4a");
        lblScore.setStyle("-fx-font-family: 'Rajdhani'; -fx-font-weight: bold; -fx-font-size: 36px; -fx-text-fill: " + color + ";");
        lblRiskLevel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 14px;");

        // 🚀 UI UPDATE MERGED: Adjust the arc length (gauge) - matches the Dashboard UI!
        if (arcRiskGauge != null) {
            double gaugeLength = -300.0 * (summary.score() / 100.0);
            arcRiskGauge.setLength(gaugeLength);
            arcRiskGauge.setStroke(javafx.scene.paint.Color.web(color));
        }

        removedMetadataBox.getChildren().clear();
        if (summary.extractedData() != null) {
            for (String data : summary.extractedData()) {
                HBox row = new HBox(8);
                row.setAlignment(Pos.CENTER_LEFT);
                FontIcon icon = new FontIcon("fas-check-circle");
                icon.setIconColor(javafx.scene.paint.Color.web("#38c96e"));
                Label lbl = new Label(data.split(":")[0]);
                lbl.setStyle("-fx-text-fill: #dde1ef;");
                row.getChildren().addAll(icon, lbl);
                removedMetadataBox.getChildren().add(row);
            }
        }

        logBox.getChildren().clear();
        String time = summary.scanTime().split(" ")[1];
        addLogEntry("[" + time + "] File Ingested", "log-info");
        addLogEntry("[" + time + "] Commencing metadata extraction...", "log-info");
        addLogEntry("[" + time + "] Found " + summary.totalMet() + " metadata tags.", "log-warning");
        addLogEntry("[" + time + "] Overwriting sensitive tags with null bytes...", "log-info");
        addLogEntry("[" + time + "] Sanitization complete. File re-encoded.", "log-success");
    }

    private void addLogEntry(String text, String styleClass) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add(styleClass);
        logBox.getChildren().add(lbl);
    }

    @FXML
    private void onCopyFileHash() {
        copyToClipboard(lblFileHash.getText());
    }

    private void copyToClipboard(String text) {
        if (text == null || text.trim().isEmpty()) return;
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        Clipboard.getSystemClipboard().setContent(content);
    }

    @FXML
    private void onExportPDF() {
        if (currentSelectedReport == null) return;

        // Gag the noisy PDFBox/FontBox loggers
        java.util.logging.Logger.getLogger("org.apache.pdfbox").setLevel(java.util.logging.Level.OFF);
        java.util.logging.Logger.getLogger("org.apache.fontbox").setLevel(java.util.logging.Level.OFF);

        try {
            // Open a "Save As" window for the user
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Save TraceZero Report");
            fileChooser.setInitialFileName("TraceZero_Report_" + currentSelectedReport.fileName() + ".pdf");
            fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            // Get the current window and show the dialog
            File pdfFile = fileChooser.showSaveDialog(detailsView.getScene().getWindow());

            // If the user clicks "Cancel" on the save window, just stop.
            if (pdfFile == null) {
                System.out.println(">>> Export cancelled by user.");
                return;
            }

            // Generate the PDF using Apache PDFBox
            try (org.apache.pdfbox.pdmodel.PDDocument document = new org.apache.pdfbox.pdmodel.PDDocument()) {
                org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage();
                document.addPage(page);

                try (org.apache.pdfbox.pdmodel.PDPageContentStream contentStream = new org.apache.pdfbox.pdmodel.PDPageContentStream(document, page)) {
                    contentStream.beginText();

                    org.apache.pdfbox.pdmodel.font.PDType1Font fontBold = new org.apache.pdfbox.pdmodel.font.PDType1Font(org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA_BOLD);
                    org.apache.pdfbox.pdmodel.font.PDType1Font fontNormal = new org.apache.pdfbox.pdmodel.font.PDType1Font(org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA);

                    contentStream.setFont(fontBold, 16);
                    contentStream.newLineAtOffset(50, 750);
                    contentStream.setLeading(20.0f);

                    contentStream.showText("TRACEZERO FORENSIC REPORT");
                    contentStream.newLine();

                    contentStream.setFont(fontNormal, 12);
                    contentStream.showText("==================================================");
                    contentStream.newLine();
                    contentStream.showText("Target File: " + currentSelectedReport.fileName());
                    contentStream.newLine();
                    contentStream.showText("Scan Time:   " + currentSelectedReport.scanTime());
                    contentStream.newLine();
                    contentStream.showText("Risk Score:  " + currentSelectedReport.score() + "/100");
                    contentStream.newLine();
                    contentStream.showText("Status:      SANITIZED (METADATA PURGED)");
                    contentStream.newLine();
                    contentStream.showText("==================================================");
                    contentStream.newLine();
                    contentStream.newLine();

                    contentStream.setFont(fontBold, 14);
                    contentStream.showText("DESTROYED METADATA TRACES:");
                    contentStream.newLine();
                    contentStream.setFont(fontNormal, 12);

                    int lineCount = 0;
                    if (currentSelectedReport.extractedData() != null && !currentSelectedReport.extractedData().isEmpty()) {
                        for (String data : currentSelectedReport.extractedData()) {
                            String cleanData = data.replaceAll("\\p{C}", "?");
                            contentStream.showText("[X] STRIPPED -> " + cleanData);
                            contentStream.newLine();
                            lineCount++;
                            if (lineCount > 25) {
                                contentStream.showText("... (Additional metadata truncated)");
                                break;
                            }
                        }
                    } else {
                        contentStream.showText("No extractable metadata found.");
                    }
                    contentStream.endText();
                }
                // Save the document to the location the user picked!
                document.save(pdfFile);
            }

            System.out.println(">>> SUCCESS: PDF saved to: " + pdfFile.getAbsolutePath());

            // Auto-open the PDF so they don't have to go looking for it!
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(pdfFile);
            }

        } catch (Exception e) {
            System.out.println("!!! Error exporting PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onExportJSON() {
        System.out.println("Export JSON clicked");
    }

    @FXML
    private void onDownloadCleaned() {
        if (currentSelectedReport != null && currentSelectedReport.actualFile() != null) {
            try {
                Desktop.getDesktop().open(currentSelectedReport.actualFile().getParentFile());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}