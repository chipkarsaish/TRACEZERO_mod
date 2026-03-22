package com.example.hellofx.controllers;

import com.example.hellofx.MainController;
import com.example.hellofx.scanner.*;
import com.example.hellofx.utils.FileTypeDetector;
import com.example.hellofx.utils.FileTypeDetector.FileType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ScanFilesController implements Initializable {

    @FXML private VBox dropZoneContainer, dropZone, scanProgressBox;
    @FXML private HBox resultsContainer;
    @FXML private VBox metadataTableRows;
    @FXML private Label lblSidebarFileName, lblRiskScore;
    @FXML private Button btnCleanAll;
    @FXML private ProgressBar riskProgressBar;

    private File selectedFile;
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        showDropZone();
    }

    private List<String> currentScanResults;

    @FXML
    private void onCleanAllClick() {
        if (selectedFile == null || mainController == null) return;

        mainController.setActiveFile(selectedFile);
        mainController.setExtractedMetadata(currentScanResults);

        mainController.navigateToCleanSanitize();
    }

    private void loadFile(File file) {
        this.selectedFile = file;

        dropZone.setVisible(false); dropZone.setManaged(false);
        scanProgressBox.setVisible(true); scanProgressBox.setManaged(true);

        new Thread(() -> {
            FileType type = FileTypeDetector.detect(file);

            // 🚀 1. ADDED ZIP TO THE SCANNER ROUTING
            switch (type) {
                case IMAGE -> currentScanResults = ImageScanner.scanImage(file);
                case PDF   -> currentScanResults = PdfScanner.scanPDF(file);
                case DOCX  -> currentScanResults = DocxScanner.scanDOCX(file);
                case ZIP   -> currentScanResults = ZipScanner.scanZIP(file); // <-- The Magic Link
                default    -> currentScanResults = List.of("Status: File type not supported");
            }

            Platform.runLater(() -> {
                lblSidebarFileName.setText(file.getName());
                metadataTableRows.getChildren().clear();

                for (String entry : currentScanResults) {
                    if (entry.contains(":")) {
                        String[] parts = entry.split(":", 2);
                        addMetadataRow(parts[0].replaceAll("\\[.*?\\] ", "").trim(), parts[1].trim());
                    } else {
                        addMetadataRow("Data Point", entry);
                    }
                }

                double score = Math.min(currentScanResults.size() * 0.2, 1.0);
                if (riskProgressBar != null) riskProgressBar.setProgress(score);
                if (lblRiskScore != null) lblRiskScore.setText((int)(score * 100) + "/100");

                showResults();

                if (com.example.hellofx.utils.SettingsManager.autoClean) {
                    System.out.println(">>> SETTINGS RULE: Auto-Clean is ON. Bypassing manual review...");
                    new Thread(() -> {
                        try { Thread.sleep(800); } catch (InterruptedException ignored) {}
                        Platform.runLater(this::onCleanAllClick);
                    }).start();
                }
            });
        }).start();
    }

    /* ── Drag & Drop ── */
    @FXML private void onDragOver(DragEvent e) {
        if (e.getDragboard().hasFiles()) {
            e.acceptTransferModes(TransferMode.COPY);
            if (!dropZone.getStyleClass().contains("drop-zone-hover")) {
                dropZone.getStyleClass().add("drop-zone-hover");
            }
        }
        e.consume();
    }

    @FXML private void onDragExited(DragEvent e) {
        dropZone.getStyleClass().remove("drop-zone-hover");
        e.consume();
    }

    @FXML private void onDragDropped(DragEvent e) {
        Dragboard db = e.getDragboard();
        if (db.hasFiles() && !db.getFiles().isEmpty()) {
            loadFile(db.getFiles().get(0));
            e.setDropCompleted(true);
        }
        dropZone.getStyleClass().remove("drop-zone-hover");
        e.consume();
    }

    @FXML private void onBrowseFiles() {
        FileChooser chooser = new FileChooser();
        File file = chooser.showOpenDialog(dropZone.getScene().getWindow());
        if (file != null) loadFile(file);
    }

    private void showDropZone() {
        resultsContainer.setVisible(false); resultsContainer.setManaged(false);
        dropZoneContainer.setVisible(true); dropZoneContainer.setManaged(true);
        dropZone.setVisible(true); dropZone.setManaged(true);
        scanProgressBox.setVisible(false); scanProgressBox.setManaged(false);
    }

    private void showResults() {
        dropZoneContainer.setVisible(false); dropZoneContainer.setManaged(false);
        resultsContainer.setVisible(true); resultsContainer.setManaged(true);
    }

    private void addMetadataRow(String type, String value) {
        HBox row = new HBox(16);
        row.getStyleClass().add("metadata-row");
        Label t = new Label(type); t.getStyleClass().add("metadata-type"); t.setMinWidth(150);
        Label v = new Label(value); v.getStyleClass().add("metadata-value"); HBox.setHgrow(v, Priority.ALWAYS);
        row.getChildren().addAll(t, v);
        metadataTableRows.getChildren().add(row);
    }
}