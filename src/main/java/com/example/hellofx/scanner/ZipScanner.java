package com.example.hellofx.scanner;

import java.io.File;
import java.io.FileInputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipScanner {

    public static List<String> scanZIP(File zipFile) {
        List<String> dataList = new ArrayList<>();
        int fileCount = 0;
        int trackerCount = 0;
        int exposedTimestamps = 0; // Track if we actually find REAL dates

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                fileCount++;
                String name = entry.getName();
                boolean isTracker = name.contains(".DS_Store") || name.contains("__MACOSX") || name.contains("Thumbs.db");
                if (isTracker) trackerCount++;

                StringBuilder alert = new StringBuilder();
                boolean hasRealMetadata = false;

                // Check if Modified Time exists AND is NOT the 1970 Epoch
                if (entry.getLastModifiedTime() != null && entry.getLastModifiedTime().toMillis() > 0) {
                    alert.append("\n   ├─ Modified: ").append(entry.getLastModifiedTime());
                    hasRealMetadata = true;
                    exposedTimestamps++;
                }

                // Check if Creation Time exists AND is NOT the 1970 Epoch
                if (entry.getCreationTime() != null && entry.getCreationTime().toMillis() > 0) {
                    alert.append("\n   ├─ Created: ").append(entry.getCreationTime());
                    hasRealMetadata = true;
                }

                if (isTracker) {
                    alert.append("\n   └─ ⚠️ WARNING: Hidden OS Tracker Detected!");
                    hasRealMetadata = true;
                }

                // ONLY add the file to the alert list if it has REAL metadata or a tracker
                if (hasRealMetadata) {
                    dataList.add("[Entry] " + name + alert.toString());
                }

                zis.closeEntry();
            }

            // Summary Header
            dataList.add(0, "=== ZIP ARCHIVE SUMMARY ===");
            dataList.add(1, "Total Files Scanned: " + fileCount);

            if (exposedTimestamps == 0 && trackerCount == 0) {
                dataList.add(2, "Status: 100% SECURE (No true metadata found)");
            } else {
                dataList.add(2, "Hidden Trackers Found: " + trackerCount);
                dataList.add(3, "Exposed Timestamps: " + exposedTimestamps);
            }
            dataList.add("---------------------------");

        } catch (Exception e) {
            dataList.add("❌ Error reading ZIP metadata: " + e.getMessage());
        }

        return dataList;
    }
}