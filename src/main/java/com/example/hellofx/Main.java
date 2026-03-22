package com.example.hellofx;

import com.example.hellofx.cleaner.DocxCleaner;
import com.example.hellofx.cleaner.ImageCleaner;
import com.example.hellofx.cleaner.PdfCleaner;
import com.example.hellofx.cleaner.ZipCleaner;
import com.example.hellofx.utils.FileTypeDetector;
import javafx.application.Application;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0) {
            // 🚀 CLI Mode: Process the file path passed as an argument
            runCLI(args[0]);
        } else {
            // 🖥️ GUI Mode: Launch the full JavaFX interface
            // We removed the setUserAgentStylesheet from here to prevent the crash!
            Application.launch(HelloApplication.class, args);
        }
    }

    private static void runCLI(String filePath) {
        System.out.println("--- TRACEZERO SENTINEL CLI ---");
        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("❌ Error: File not found at " + filePath);
            return;
        }

        FileTypeDetector.FileType type = FileTypeDetector.detect(file);
        System.out.println("Detected Type: " + type);

        try {
            File cleaned = null;
            // ... inside runCLI method ...
            switch (type) {
                case IMAGE -> cleaned = ImageCleaner.cleanImage(file);
                case PDF   -> cleaned = PdfCleaner.cleanPDF(file);
                case DOCX  -> cleaned = DocxCleaner.cleanDOCX(file);
                case ZIP   -> cleaned = ZipCleaner.cleanZIP(file); // 🚀 Wire the ZIP logic
                default -> System.out.println("🚫 Unsupported file type.");
            }

            if (cleaned != null && cleaned.exists()) {
                System.out.println("✅ SUCCESS: Sanitized file saved at: " + cleaned.getAbsolutePath());
            } else {
                System.out.println("❌ FAILED: Metadata removal interrupted.");
            }
        } catch (Exception e) {
            System.out.println("!!! Critical Error: " + e.getMessage());
        }
    }
}