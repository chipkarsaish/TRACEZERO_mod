package com.example.hellofx.controllers;

import com.example.hellofx.MainController;
import com.example.hellofx.cleaner.*;
import com.example.hellofx.utils.FileTypeDetector;
import com.example.hellofx.utils.FileTypeDetector.FileType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class CleanSanitizeController implements Initializable {

    @FXML
    private Label lblStatusHeader;
    @FXML
    private Button btnCleanNow;
    @FXML
    private Button btnDownload;
    @FXML
    private ListView<String> metadataListView;

    private MainController mainController;
    private File fileToClean;
    private List<String> metadataToClean;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        this.fileToClean = mainController.getActiveFile();
        this.metadataToClean = mainController.getExtractedMetadata();

        if (fileToClean != null) {
            lblStatusHeader.setText("READY: " + fileToClean.getName());
        }

        if (metadataToClean != null && metadataListView != null) {
            metadataListView.getItems().clear();
            metadataListView.getItems().addAll(metadataToClean);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @FXML
    private void onCleanNow() {
        if (fileToClean == null) return;

        btnCleanNow.setDisable(true);
        btnCleanNow.setText("⌛ PURGING...");

        FileType type = FileTypeDetector.detect(fileToClean);

        new Thread(() -> {
            File cleanedFile = null;
            try {
                // 🚀 1. Run the specialized cleaners with a safety fallback
                switch (type) {
                    case IMAGE -> cleanedFile = ImageCleaner.cleanImage(fileToClean);
                    case PDF   -> cleanedFile = PdfCleaner.cleanPDF(fileToClean);
                    case DOCX  -> cleanedFile = DocxCleaner.cleanDOCX(fileToClean);
                    case ZIP   -> cleanedFile = ZipCleaner.cleanZIP(fileToClean);
                    default -> {
                        System.out.println("🚫 Unsupported file type for cleaning.");
                        Platform.runLater(() -> {
                            lblStatusHeader.setText("Error: Unsupported File");
                            btnCleanNow.setDisable(false);
                            btnCleanNow.setText("RETRY");
                        });
                        return; // Exit the thread completely
                    }
                }

                File finalResult = cleanedFile;

                // 🚀 2. OBEY SETTINGS: Backup vs Overwrite
                if (finalResult != null && finalResult.exists()) {
                    if (!com.example.hellofx.utils.SettingsManager.backupOriginals) {
                        System.out.println(">>> SETTINGS RULE: Overwriting original file...");
                        File original = fileToClean;
                        String originalPath = original.getAbsolutePath();

                        if (original.delete()) {
                            File overwrittenFile = new File(originalPath);
                            boolean success = finalResult.renameTo(overwrittenFile);
                            if (success) {
                                finalResult = overwrittenFile;
                            } else {
                                System.out.println("⚠️ Warning: Could not rename file. Keeping backup.");
                            }
                        }
                    }
                }

                final File fileForUI = finalResult;

                Platform.runLater(() -> {
                    if (fileForUI != null && fileForUI.exists()) {
                        lblStatusHeader.setText("✓ METADATA DESTROYED");

                        if (metadataListView != null) {
                            metadataListView.getItems().clear();
                            metadataListView.getItems().add("[ ALL TRACES SUCCESSFULLY REMOVED ]");
                        }

                        btnDownload.setVisible(true);
                        btnCleanNow.setText("SUCCESS");

                        com.example.hellofx.utils.SettingsManager.showOSNotification(
                                "TraceZero Sentinel",
                                "Sanitization Complete: " + fileForUI.getName()
                        );

                        btnDownload.setOnAction(e -> {
                            if (mainController != null) mainController.showReports();
                        });

                    } else {
                        lblStatusHeader.setText("Error: Sanitization Failed");
                        btnCleanNow.setDisable(false);
                        btnCleanNow.setText("RETRY");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (com.example.hellofx.utils.SettingsManager.deleteTemp) {
                    System.out.println(">>> SETTINGS RULE: Purging temporary memory caches...");
                    System.gc();
                }
            }
        }).start();
    }
}