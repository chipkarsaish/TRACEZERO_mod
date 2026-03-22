package com.example.hellofx.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SettingsManager {

    private static final String SETTINGS_PATH = "logs/settings.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // ── PERSISTENT SETTINGS ──
    public static boolean folderMonitor = true;
    public static boolean autoClean = false;
    public static boolean desktopNotif = true;
    public static boolean backupOriginals = true;
    public static boolean deleteTemp = true;
    
    // ── NEW: Periodic Sweep Settings ──
    public static boolean periodicScan = true;
    public static int periodicScanInterval = 1; // Default 1 minute

    // We store the folders here now so they can be saved to the JSON file
    public static List<String> watchedFolders = new ArrayList<>();
    public static boolean soundAlert;

    /** Saves all current static variables to settings.json */
    public static void save() {
        try {
            File file = new File(SETTINGS_PATH);
            file.getParentFile().mkdirs();

            SettingsData data = new SettingsData();
            data.folderMonitor = folderMonitor;
            data.autoClean = autoClean;
            data.desktopNotif = desktopNotif;
            data.backupOriginals = backupOriginals;
            data.deleteTemp = deleteTemp;
            data.watchedFolders = watchedFolders;
            data.periodicScan = periodicScan;
            data.periodicScanInterval = periodicScanInterval;

            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(data, writer);
            }
        } catch (IOException e) {
            System.err.println("!!! Failed to save settings: " + e.getMessage());
        }
    }

    /** Loads settings from JSON on startup */
    public static void load() {
        File file = new File(SETTINGS_PATH);
        if (!file.exists()) return;

        try (FileReader reader = new FileReader(file)) {
            SettingsData data = gson.fromJson(reader, SettingsData.class);
            if (data != null) {
                folderMonitor = data.folderMonitor;
                autoClean = data.autoClean;
                desktopNotif = data.desktopNotif;
                backupOriginals = data.backupOriginals;
                deleteTemp = data.deleteTemp;
                watchedFolders = (data.watchedFolders != null) ? data.watchedFolders : new ArrayList<>();
                
                // Initialize new fields safely in case of old config files
                periodicScan = (data.periodicScan != null) ? data.periodicScan : true;
                periodicScanInterval = (data.periodicScanInterval > 0) ? data.periodicScanInterval : 1;
                
                System.out.println(">>> Settings & Folders loaded successfully.");
            }
        } catch (IOException e) {
            System.err.println("!!! Failed to load settings: " + e.getMessage());
        }
    }

    public static void printStatus() {

    }

    // A simple helper class for Gson to map the data
    private static class SettingsData {
        boolean folderMonitor, autoClean, desktopNotif, backupOriginals, deleteTemp;
        Boolean periodicScan;
        int periodicScanInterval;
        List<String> watchedFolders;
    }

    // 🚀 Native OS Notification Engine
    public static void showOSNotification(String title, String message) {
        if (!desktopNotif || !java.awt.SystemTray.isSupported()) return;

        try {
            java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();
            java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g2d = image.createGraphics();
            g2d.setColor(java.awt.Color.RED);
            g2d.fillRect(0, 0, 16, 16);
            g2d.dispose();

            java.awt.TrayIcon trayIcon = new java.awt.TrayIcon(image, "TraceZero Sentinel");
            trayIcon.setImageAutoSize(true);
            tray.add(trayIcon);
            trayIcon.displayMessage(title, message, java.awt.TrayIcon.MessageType.INFO);

            new Thread(() -> {
                try { Thread.sleep(10000); tray.remove(trayIcon); } catch (Exception ignored) {}
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}