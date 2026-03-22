package com.example.hellofx.core;

import javafx.application.Platform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class LogManager {

    private static LogManager instance;
    private final List<String> recentLogs;
    private Consumer<String> onLogListener; // 🔥 New: Listener for the UI

    private LogManager() {
        this.recentLogs = Collections.synchronizedList(new ArrayList<>());
    }

    public static synchronized LogManager getInstance() {
        if (instance == null) {
            instance = new LogManager();
        }
        return instance;
    }

    // 🔥 New: The UI uses this to "listen" for new events
    public void setOnLogListener(Consumer<String> listener) {
        this.onLogListener = listener;
    }

    public void log(String message) {
        System.out.println(message);
        recentLogs.add(message);

        if (recentLogs.size() > 100) {
            recentLogs.remove(0);
        }

        // 🔥 Trigger the UI update automatically
        if (onLogListener != null) {
            Platform.runLater(() -> onLogListener.accept(message));
        }
    }

    public List<String> getRecentLogs() {
        return new ArrayList<>(recentLogs);
    }
}