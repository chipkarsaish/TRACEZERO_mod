package com.example.hellofx.core;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class AppStats {
    private AtomicInteger totalFilesDetected;
    private AtomicInteger filesAutoCleaned;
    private AtomicInteger gpsMetadataFound;
    private AtomicLong totalProcessingTime;

    public AppStats() {
        this.totalFilesDetected = new AtomicInteger(0);
        this.filesAutoCleaned = new AtomicInteger(0);
        this.gpsMetadataFound = new AtomicInteger(0);
        this.totalProcessingTime = new AtomicLong(0);
    }

    public int getTotalFilesDetected() {
        return totalFilesDetected.get();
    }

    public void incrementFilesDetected() {
        totalFilesDetected.incrementAndGet();
    }

    public int getFilesAutoCleaned() {
        return filesAutoCleaned.get();
    }

    public void incrementFilesCleaned() {
        filesAutoCleaned.incrementAndGet();
    }

    public int getGpsMetadataFound() {
        return gpsMetadataFound.get();
    }

    public void incrementGpsDetected() {
        gpsMetadataFound.incrementAndGet();
    }

    public long getTotalProcessingTime() {
        return totalProcessingTime.get();
    }

    public void addProcessingTime(long time) {
        totalProcessingTime.addAndGet(time);
    }
}
