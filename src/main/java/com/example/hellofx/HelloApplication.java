package com.example.hellofx;

import atlantafx.base.theme.Dracula;
import com.example.hellofx.utils.SettingsManager;
import com.example.hellofx.utils.WindowsTitleBar;
import com.example.hellofx.watcher.FolderWatcher;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.Objects;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // 1. Load Settings and Start Watcher
        SettingsManager.load();
        FolderWatcher.getInstance().startWatching();

        // 2. Setup Theme and UI
        Application.setUserAgentStylesheet(new Dracula().getUserAgentStylesheet());
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("MainLayout.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);

        stage.setTitle("TRACEZERO");

        // JavaFX Window Icon
        try {
            stage.getIcons().add(new Image(Objects.requireNonNull(HelloApplication.class.getResourceAsStream("views/sec.png"))));
        } catch (Exception ignored) {}

        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);

        // 3. Setup System Tray with your sec.png
        createSystemTray(stage);

        stage.show();
    }

    private void createSystemTray(Stage stage) {
        if (!java.awt.SystemTray.isSupported()) return;

        java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();

        // 🚀 Load your real icon for the tray
        java.awt.Image trayImage = null;
        try {
            trayImage = ImageIO.read(Objects.requireNonNull(HelloApplication.class.getResourceAsStream("views/sec.png")));
        } catch (Exception e) {
            // Fallback to red square if image fails to load
            java.awt.image.BufferedImage fallback = new java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = fallback.createGraphics();
            g.setColor(java.awt.Color.RED);
            g.fillRect(0, 0, 16, 16);
            g.dispose();
            trayImage = fallback;
        }

        java.awt.TrayIcon trayIcon = new java.awt.TrayIcon(trayImage, "TraceZero Sentinel");
        trayIcon.setImageAutoSize(true);

        // 🚀 THE FIX: Single-click to open the app
        trayIcon.addActionListener(e -> Platform.runLater(() -> {
            stage.show();
            stage.toFront(); // Bring to the very front of other windows
        }));

        java.awt.PopupMenu menu = new java.awt.PopupMenu();
        java.awt.MenuItem showItem = new java.awt.MenuItem("Open TraceZero");
        showItem.addActionListener(e -> Platform.runLater(stage::show));

        java.awt.MenuItem exitItem = new java.awt.MenuItem("Exit Completely");
        exitItem.addActionListener(e -> {
            FolderWatcher.getInstance().stopWatching();
            System.exit(0);
        });

        menu.add(showItem);
        menu.addSeparator();
        menu.add(exitItem);
        trayIcon.setPopupMenu(menu);

        try {
            tray.add(trayIcon);
            Platform.setImplicitExit(false);

            stage.setOnCloseRequest(e -> {
                e.consume();
                stage.hide();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}