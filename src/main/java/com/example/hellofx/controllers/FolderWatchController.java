package com.example.hellofx.controllers;

import com.example.hellofx.core.LogManager;
import com.example.hellofx.core.StatisticsManager;
import com.example.hellofx.utils.SettingsManager;
import com.example.hellofx.watcher.FolderWatcher;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class FolderWatchController implements Initializable {

    @FXML private VBox folderListContainer;
    @FXML private Label lblFolderCount, lblStatusText;

    @FXML private StackPane toggleStatus, toggleAutoClean, toggleDelete, togglePeriodic;
    @FXML private Rectangle trackStatus, trackAutoClean, trackDelete, trackPeriodic;
    @FXML private Circle thumbStatus, thumbAutoClean, thumbDelete, thumbPeriodic;
    @FXML private ComboBox<String> comboInterval;

    @FXML private Label lblDetected, lblCleaned, lblGpsFound, lblAvgTime;
    @FXML private VBox logContainer;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 🔥 Load all settings from JSON first
        SettingsManager.load();

        // Sync UI toggles to the loaded settings
        setToggleInitialState(trackStatus, thumbStatus, SettingsManager.folderMonitor);
        setToggleInitialState(trackAutoClean, thumbAutoClean, SettingsManager.autoClean);
        setToggleInitialState(trackDelete, thumbDelete, !SettingsManager.backupOriginals);
        setToggleInitialState(trackPeriodic, thumbPeriodic, SettingsManager.periodicScan);

        // Map int interval to ComboBox String
        comboInterval.setValue(SettingsManager.periodicScanInterval + " Minute" + (SettingsManager.periodicScanInterval > 1 ? "s" : ""));

        updateStatusLabel(SettingsManager.folderMonitor);
        refreshFolderList();
        refreshStatistics();

        // Listen for background events to update UI live
        LogManager.getInstance().setOnLogListener(msg -> {
            addLog(msg, msg.contains("[✓]") ? "log-success" : "log-info");
            refreshStatistics();
        });
    }

    private void refreshStatistics() {
        StatisticsManager stats = StatisticsManager.getInstance();
        lblDetected.setText(String.valueOf(stats.getTotalFilesDetected()));
        lblCleaned.setText(String.valueOf(stats.getFilesCleaned()));
        lblGpsFound.setText(String.valueOf(stats.getGpsMetadataFound()));

        double avg = stats.getAverageCleanTime();
        lblAvgTime.setText(avg > 1000 ? String.format("%.1fs", avg / 1000.0) : String.format("%.0fms", avg));
    }

    private void updateStatusLabel(boolean isActive) {
        lblStatusText.setText(isActive ? "ACTIVE" : "OFFLINE");
        lblStatusText.setStyle("-fx-text-fill: " + (isActive ? "#38c96e" : "#ff4a4a") + "; -fx-font-weight: bold; -fx-font-size: 11px;");
    }

    private void setToggleInitialState(Rectangle track, Circle thumb, boolean isOn) {
        thumb.setTranslateX(isOn ? 21 : 3);
        track.setFill(Color.web(isOn ? "#4a1818" : "#2a2e40"));
        thumb.setFill(Color.web(isOn ? "#ff4a4a" : "#6b7280"));
    }

    @FXML private void onAddFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        File dir = chooser.showDialog(folderListContainer.getScene().getWindow());
        if (dir != null) {
            FolderWatcher.getInstance().addWatchedFolder(dir.getAbsolutePath());
            refreshFolderList();
            addLog("[+] Watch directory added: " + dir.getName(), "log-info");
        }
    }

    private void removeFolder(String path) {
        FolderWatcher.getInstance().removeWatchedFolder(path);
        refreshFolderList();
        addLog("[-] Watch directory removed: " + new File(path).getName(), "log-warning");
    }

    private void refreshFolderList() {
        folderListContainer.getChildren().clear();
        // 🚀 FIX: Reads directly from SettingsManager now
        int count = SettingsManager.watchedFolders.size();
        lblFolderCount.setText("Watched Folders (" + count + ")");
        for (String path : SettingsManager.watchedFolders) {
            folderListContainer.getChildren().add(buildFolderRow(path));
        }
    }

    private HBox buildFolderRow(String path) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(9, 12, 9, 12));
        row.setStyle("-fx-background-color: #1a1d2e; -fx-background-radius: 8; -fx-border-color: #2a2e40; -fx-border-width: 1; -fx-border-radius: 8; -fx-cursor: hand;");

        Label pathLabel = new Label(path);
        pathLabel.setStyle("-fx-text-fill: #aab0c6; -fx-font-size: 12px;");
        pathLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(pathLabel, Priority.ALWAYS);

        Button delBtn = new Button("🗑");
        delBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6b7280; -fx-cursor: hand;");
        delBtn.setOnAction(e -> removeFolder(path));

        row.getChildren().addAll(new Label("📁"), pathLabel, delBtn);
        return row;
    }

    @FXML private void onToggleStatus(MouseEvent e) {
        SettingsManager.folderMonitor = toggleState(trackStatus, thumbStatus);
        updateStatusLabel(SettingsManager.folderMonitor);
        SettingsManager.save(); // 🔥 Save preference
        if (SettingsManager.folderMonitor) FolderWatcher.getInstance().startWatching();
        else FolderWatcher.getInstance().stopWatching();
    }

    @FXML private void onToggleAutoClean(MouseEvent e) {
        SettingsManager.autoClean = toggleState(trackAutoClean, thumbAutoClean);
        SettingsManager.save(); // 🔥 Save preference
    }

    @FXML private void onToggleDelete(MouseEvent e) {
        SettingsManager.backupOriginals = !toggleState(trackDelete, thumbDelete);
        SettingsManager.save(); // 🔥 Save preference
    }

    @FXML private void onTogglePeriodic(MouseEvent e) {
        SettingsManager.periodicScan = toggleState(trackPeriodic, thumbPeriodic);
        SettingsManager.save(); 
        FolderWatcher.getInstance().restartScheduler();
    }

    @FXML private void onIntervalChanged() {
        String val = comboInterval.getValue();
        if (val != null) {
            int mins = Integer.parseInt(val.split(" ")[0]);
            SettingsManager.periodicScanInterval = mins;
            SettingsManager.save();
            FolderWatcher.getInstance().restartScheduler();
        }
    }

    private boolean toggleState(Rectangle track, Circle thumb) {
        boolean isOn = thumb.getTranslateX() > 10;
        TranslateTransition tt = new TranslateTransition(Duration.millis(150), thumb);
        boolean becomesOn = !isOn;
        tt.setToX(becomesOn ? 21 : 3);
        track.setFill(Color.web(becomesOn ? "#4a1818" : "#2a2e40"));
        thumb.setFill(Color.web(becomesOn ? "#ff4a4a" : "#6b7280"));
        tt.play();
        return becomesOn;
    }

    private void addLog(String message, String styleClass) {
        Platform.runLater(() -> {
            Label logMsg = new Label(message);
            logMsg.getStyleClass().add(styleClass);
            logContainer.getChildren().add(0, logMsg);
            if (logContainer.getChildren().size() > 50) logContainer.getChildren().remove(50);
        });
    }
}