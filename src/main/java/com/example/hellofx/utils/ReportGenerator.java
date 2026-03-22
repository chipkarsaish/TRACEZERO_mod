package com.example.hellofx.utils;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportGenerator {

    public static void generateAndOpenReport(File originalFile, List<String> metadata) {
        try {
            // 1. Create a "reports" folder in your project root if it doesn't exist
            File reportsDir = new File("reports");
            if (!reportsDir.exists()) {
                reportsDir.mkdir();
            }

            // 2. Create a unique filename with a timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File reportFile = new File(reportsDir, "TraceZero_Report_" + originalFile.getName() + "_" + timestamp + ".txt");

            // 3. Write the beautifully formatted text
            try (FileWriter writer = new FileWriter(reportFile)) {
                writer.write("==================================================\n");
                writer.write("          TRACEZERO SECURITY AUDIT REPORT         \n");
                writer.write("==================================================\n\n");
                writer.write("TARGET FILE: " + originalFile.getAbsolutePath() + "\n");
                writer.write("TIMESTAMP:   " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
                writer.write("STATUS:      SANITIZED (METADATA PURGED)\n\n");
                writer.write("==================================================\n");
                writer.write("DESTROYED METADATA TRACES:\n");
                writer.write("==================================================\n");

                if (metadata != null && !metadata.isEmpty()) {
                    for (String data : metadata) {
                        writer.write("[X] STRIPPED -> " + data + "\n");
                    }
                } else {
                    writer.write("No extractable metadata found.\n");
                }

                writer.write("\n==================================================\n");
                writer.write("END OF REPORT - FILE IS SECURE\n");
            }

            // 4. THE HACKATHON FLEX: Auto-open the file using the OS Default Text Editor
            System.out.println(">>> Report Generated at: " + reportFile.getAbsolutePath());
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(reportFile);
            }

        } catch (Exception e) {
            System.out.println("!!! Error generating report: " + e.getMessage());
            e.printStackTrace();
        }
    }
}