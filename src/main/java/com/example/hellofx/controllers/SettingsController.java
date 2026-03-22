package com.example.hellofx.controllers;

import com.example.hellofx.MainController;
import com.example.hellofx.utils.SettingsManager;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    private MainController mainController;
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // Automation
    @FXML private Rectangle trackFolderMonitor;  @FXML private Circle thumbFolderMonitor;
    @FXML private Rectangle trackAutoClean;      @FXML private Circle thumbAutoClean;

    // System Integration
    @FXML private Rectangle trackDesktopNotif;   @FXML private Circle thumbDesktopNotif;
    @FXML private Rectangle trackBackup;         @FXML private Circle thumbBackup;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Sync UI to SettingsManager on load
        syncToggleToState(SettingsManager.folderMonitor, trackFolderMonitor, thumbFolderMonitor);
        syncToggleToState(SettingsManager.autoClean, trackAutoClean, thumbAutoClean);
        syncToggleToState(SettingsManager.desktopNotif, trackDesktopNotif, thumbDesktopNotif);
        syncToggleToState(SettingsManager.backupOriginals, trackBackup, thumbBackup);
    }

    // ── Click Handlers ─────────────────────────────────────────────────
    @FXML private void onToggleFolderMonitor(MouseEvent e) { SettingsManager.folderMonitor = toggleState(trackFolderMonitor, thumbFolderMonitor); SettingsManager.printStatus(); }
    @FXML private void onToggleAutoClean(MouseEvent e)     { SettingsManager.autoClean = toggleState(trackAutoClean, thumbAutoClean); SettingsManager.printStatus(); }
    @FXML private void onToggleDesktopNotif(MouseEvent e)  { SettingsManager.desktopNotif = toggleState(trackDesktopNotif, thumbDesktopNotif); SettingsManager.printStatus(); }
    @FXML private void onToggleBackup(MouseEvent e)        { SettingsManager.backupOriginals = toggleState(trackBackup, thumbBackup); SettingsManager.printStatus(); }

    // ── Real Theme Selection (Hackathon trick) ──────────────────────────
    @FXML private void onSelectDarkTactical() {
        if (trackFolderMonitor.getScene() != null) {
            // Revert back to standard dark gray/purple accents
            trackFolderMonitor.getScene().getRoot().setStyle("");
            System.out.println(">>> Theme changed to Dark Tactical");
        }
    }

    @FXML private void onSelectRedAlert() {
        if (trackFolderMonitor.getScene() != null) {
            // Force a deep red tint over the application root!
            trackFolderMonitor.getScene().getRoot().setStyle("-fx-base: #1a0505; -fx-background: #1a0505; -fx-control-inner-background: #3d0a0a;");
            System.out.println(">>> Theme changed to Red Alert");
        }
    }

    // ── Animation & State Logic ──────────────────────────────────────────
    private boolean toggleState(Rectangle track, Circle thumb) {
        boolean isCurrentlyOn = thumb.getTranslateX() > 10;
        boolean willBeOn = !isCurrentlyOn;

        TranslateTransition tt = new TranslateTransition(Duration.millis(150), thumb);
        if (isCurrentlyOn) {
            tt.setToX(3);
            track.setFill(javafx.scene.paint.Color.web("#2a2e40"));
            thumb.setFill(javafx.scene.paint.Color.web("#6b7280"));
        } else {
            tt.setToX(21);
            track.setFill(javafx.scene.paint.Color.web("#4a1818"));
            thumb.setFill(javafx.scene.paint.Color.web("#ff4a4a"));
        }
        tt.play();

        return willBeOn;
    }

    private void syncToggleToState(boolean state, Rectangle track, Circle thumb) {
        if (state) {
            thumb.setTranslateX(21);
            track.setFill(javafx.scene.paint.Color.web("#4a1818"));
            thumb.setFill(javafx.scene.paint.Color.web("#ff4a4a"));
        } else {
            thumb.setTranslateX(3);
            track.setFill(javafx.scene.paint.Color.web("#2a2e40"));
            thumb.setFill(javafx.scene.paint.Color.web("#6b7280"));
        }
    }
}