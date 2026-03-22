package com.example.hellofx;

import com.example.hellofx.controllers.CleanSanitizeController;
import com.example.hellofx.controllers.DashboardController;
import com.example.hellofx.controllers.ReportsController;
import com.example.hellofx.controllers.ScanFilesController;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private HBox riskAlertBanner;
    @FXML private HBox headerBar;

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML private ToggleButton btnDashboard;
    @FXML private ToggleButton btnScanFiles;
    @FXML private ToggleButton btnClean;
    @FXML private ToggleButton btnFolderWatch;
    @FXML private ToggleButton btnReports;
    @FXML private ToggleButton btnSettings;

    private final ToggleGroup navGroup = new ToggleGroup();

    // ── SHARED DATA LAYER ──────────────────────────────────────
    private File activeFile;
    private List<String> extractedMetadata;

    public void setActiveFile(File file) { this.activeFile = file; }
    public File getActiveFile() { return activeFile; }

    public void setExtractedMetadata(List<String> metadata) { this.extractedMetadata = metadata; }
    public List<String> getExtractedMetadata() { return this.extractedMetadata; }
    // ───────────────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Assign all buttons to ONE group
        btnDashboard.setToggleGroup(navGroup);
        btnScanFiles.setToggleGroup(navGroup);
        btnClean.setToggleGroup(navGroup);
        btnFolderWatch.setToggleGroup(navGroup);
        btnReports.setToggleGroup(navGroup);
        btnSettings.setToggleGroup(navGroup);

        // Default page load (this also handles the initial button highlight)
        showDashboard();

        // Startup animation for the risk banner
        if (riskAlertBanner != null) {
            pulseNode(riskAlertBanner, 3);
        }

        // Window Dragging & double click to maximize
        if (headerBar != null) {
            headerBar.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });
            headerBar.setOnMouseDragged(event -> {
                Stage stage = (Stage) headerBar.getScene().getWindow();
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            });
            headerBar.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    maximizeWindow();
                }
            });
        }
    }

    private void pulseNode(Node node, int cycles) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(node.scaleXProperty(), 1.0),
                        new KeyValue(node.scaleYProperty(), 1.0)),
                new KeyFrame(Duration.millis(300),
                        new KeyValue(node.scaleXProperty(), 1.08),
                        new KeyValue(node.scaleYProperty(), 1.08)),
                new KeyFrame(Duration.millis(600),
                        new KeyValue(node.scaleXProperty(), 1.0),
                        new KeyValue(node.scaleYProperty(), 1.0))
        );
        timeline.setCycleCount(cycles);
        timeline.play();
    }

    // ── Bulletproof View Switching & UI Highlighting ────────────

    private void setActiveButton(ToggleButton activeBtn) {
        // 1. Wipe the active style from EVERY button to prevent "ghosting"
        btnDashboard.getStyleClass().remove("sidebar-btn-active");
        btnScanFiles.getStyleClass().remove("sidebar-btn-active");
        btnClean.getStyleClass().remove("sidebar-btn-active");
        btnFolderWatch.getStyleClass().remove("sidebar-btn-active");
        btnReports.getStyleClass().remove("sidebar-btn-active");
        btnSettings.getStyleClass().remove("sidebar-btn-active");

        // 2. Apply it ONLY to the selected button
        if (activeBtn != null) {
            activeBtn.setSelected(true); // Tell the ToggleGroup this is the active one
            if (!activeBtn.getStyleClass().contains("sidebar-btn-active")) {
                activeBtn.getStyleClass().add("sidebar-btn-active"); // Apply the CSS
            }
        }
    }

    @FXML private void showDashboard()   { setActiveButton(btnDashboard); loadView("views/dashboard.fxml"); }
    @FXML private void showScanFiles()   { setActiveButton(btnScanFiles); loadView("views/scan-files.fxml"); }
    @FXML private void showClean()       { setActiveButton(btnClean); loadView("views/clean-sanitize.fxml"); }
    @FXML private void showFolderWatch() { setActiveButton(btnFolderWatch); loadView("views/folder-watch.fxml"); }
    @FXML private void showSettings()    { setActiveButton(btnSettings); loadView("views/settings.fxml"); }

    // This MUST be public so the Clean page can trigger it
    @FXML
    public void showReports() {
        setActiveButton(btnReports);
        loadView("views/reports.fxml");
    }

    @FXML private void toggleMenu() {}
    @FXML private void openSettings() { showSettings(); }

    @FXML
    private void minimizeWindow() {
        if (headerBar != null) {
            Stage stage = (Stage) headerBar.getScene().getWindow();
            stage.setIconified(true);
        }
    }

    @FXML
    private void maximizeWindow() {
        if (headerBar != null) {
            Stage stage = (Stage) headerBar.getScene().getWindow();
            stage.setMaximized(!stage.isMaximized());
        }
    }

    @FXML
    private void closeWindow() {
        if (headerBar != null) {
            Stage stage = (Stage) headerBar.getScene().getWindow();
            stage.hide();
        }
    }

    // ── Navigation Triggers for other Controllers ───────────────

    public void navigateToCleanSanitize() {
        Platform.runLater(this::showClean);
    }

    public void navigateToScanFiles() {
        Platform.runLater(this::showScanFiles);
    }

    public void navigateToFolderWatch() {
        Platform.runLater(this::showFolderWatch);
    }

    // ── Loader & Dependency Injection ───────────────────────────

    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Node view = loader.load();

            Object controller = loader.getController();

            // Inject MainController into whichever view just loaded so they can talk to each other
            if (controller instanceof ScanFilesController sfc) {
                sfc.setMainController(this);
            }
            if (controller instanceof DashboardController dc) {
                dc.setMainController(this);
            }
            if (controller instanceof CleanSanitizeController csc) {
                csc.setMainController(this);
            }
            if (controller instanceof ReportsController rc) {
                rc.setMainController(this);
            }
            if (controller instanceof com.example.hellofx.controllers.SettingsController sc) {
                sc.setMainController(this);
            }

            contentArea.getChildren().setAll(view);

        } catch (IOException e) {
            System.err.println("!!! Failed to load view: " + fxmlFile);
            e.printStackTrace();
        }
    }
}