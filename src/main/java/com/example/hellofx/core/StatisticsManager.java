package com.example.hellofx.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class StatisticsManager {

    private static final String LOGS_DIR = "logs";
    private static final String STATS_FILE = LOGS_DIR + "/statistics.json";
    private static StatisticsManager instance;

    private final Gson gson;
    private AppStats currentStats;

    private StatisticsManager() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        loadStatistics();
    }

    public static synchronized StatisticsManager getInstance() {
        if (instance == null) {
            instance = new StatisticsManager();
        }
        return instance;
    }

    public synchronized void loadStatistics() {
        File dir = new File(LOGS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(STATS_FILE);
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                currentStats = gson.fromJson(reader, AppStats.class);
            } catch (IOException e) {
                System.err.println("[Stats] Failed to read statistics.json. Reverting to empty stats.");
                currentStats = new AppStats();
            }
        } else {
            currentStats = new AppStats();
            saveStatistics();
        }

        if (currentStats == null) {
            currentStats = new AppStats();
        }
    }

    public synchronized void saveStatistics() {
        File dir = new File(LOGS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try (FileWriter writer = new FileWriter(STATS_FILE)) {
            gson.toJson(currentStats, writer);
        } catch (IOException e) {
            System.err.println("[Stats] Failed to write to statistics.json.");
        }
    }

    public void incrementFilesDetected() {
        currentStats.incrementFilesDetected();
        saveStatistics();
        System.out.println("[Stats] File detected count updated");
    }

    public void incrementFilesCleaned() {
        currentStats.incrementFilesCleaned();
        saveStatistics();
        System.out.println("[Stats] File cleaned count updated");
    }

    public void incrementGpsDetected() {
        currentStats.incrementGpsDetected();
        saveStatistics();
        System.out.println("[Stats] GPS metadata detection updated");
    }

    public void recordProcessingTime(long milliseconds) {
        currentStats.addProcessingTime(milliseconds);
        saveStatistics();
        System.out.println("[Stats] Average clean time recalculated");
    }

    public int getTotalFilesDetected() {
        return currentStats.getTotalFilesDetected();
    }

    public int getFilesCleaned() {
        return currentStats.getFilesAutoCleaned();
    }

    public int getGpsMetadataFound() {
        return currentStats.getGpsMetadataFound();
    }

    public double getAverageCleanTime() {
        int cleaned = getFilesCleaned();
        if (cleaned == 0) return 0.0;
        return (double) currentStats.getTotalProcessingTime() / cleaned;
    }
}
