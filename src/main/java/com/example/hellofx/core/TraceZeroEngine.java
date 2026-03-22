package com.example.hellofx.core;

import com.example.hellofx.cleaner.*;
import com.example.hellofx.scanner.*;
import com.example.hellofx.utils.FileTypeDetector;
import com.example.hellofx.utils.FileTypeDetector.FileType;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TraceZeroEngine {

    public static void main(String[] args) {
        // Silence loggers to keep the terminal clean
        Logger.getLogger("org.apache.pdfbox").setLevel(Level.SEVERE);
        System.setProperty("log4j2.loggerContextFactory", "org.apache.logging.log4j.simple.SimpleLoggerContextFactory");

        // Target file path (Change this string to test different files!)
        String testFilePath = "F:/Demo/Cognifyz_Internship_Khagesh_Gajarushi.zip";

        File targetFile = new File(testFilePath);

        if (!targetFile.exists()) {
            System.out.println("❌ File not found: " + testFilePath);
            return;
        }

        System.out.println("=== TRACEZERO ENGINE INITIATED ===");
        System.out.println("Target File: " + targetFile.getName());

        FileType type = FileTypeDetector.detect(targetFile);
        System.out.println("Detected Type: " + type);
        System.out.println("--------------------------------");

        switch (type) {
            case IMAGE:
                processImage(targetFile);
                break;
            case PDF:
                processPDF(targetFile);
                break;
            case DOCX:
                processDOCX(targetFile);
                break;
            case ZIP: // 🚀 Added ZIP routing here!
                processZIP(targetFile);
                break;
            case UNSUPPORTED:
                System.out.println("🚫 File type not supported.");
                break;
        }
    }

    private static void processImage(File imageFile) {
        System.out.println("🔍 SCANNING IMAGE...");
        List<String> data = ImageScanner.scanImage(imageFile);
        printResults(data);

        System.out.println("🧹 CLEANING IMAGE...");
        File cleanFile = ImageCleaner.cleanImage(imageFile);
        handleResult(cleanFile);
    }

    private static void processPDF(File pdfFile) {
        System.out.println("🔍 SCANNING PDF...");
        List<String> data = PdfScanner.scanPDF(pdfFile);
        printResults(data);

        System.out.println("🧹 CLEANING PDF...");
        File cleanFile = PdfCleaner.cleanPDF(pdfFile);
        handleResult(cleanFile);
    }

    private static void processDOCX(File docxFile) {
        System.out.println("🔍 SCANNING DOCX...");
        List<String> data = DocxScanner.scanDOCX(docxFile);
        printResults(data);

        System.out.println("🧹 CLEANING DOCX...");
        File cleanFile = DocxCleaner.cleanDOCX(docxFile);
        handleResult(cleanFile);
    }

    // 🚀 Added the ZIP Processing Method
    private static void processZIP(File zipFile) {
        System.out.println("🔍 SCANNING ZIP...");
        List<String> data = ZipScanner.scanZIP(zipFile);
        printResults(data);

        System.out.println("🧹 CLEANING ZIP...");
        File cleanFile = ZipCleaner.cleanZIP(zipFile);
        handleResult(cleanFile);
    }

    // --- Helper Methods to reduce repeated code ---

    private static void printResults(List<String> data) {
        if (data.isEmpty()) {
            System.out.println("No hidden metadata found.");
        } else {
            data.forEach(System.out::println);
        }
        System.out.println("--------------------------------");
    }

    private static void handleResult(File cleanFile) {
        if (cleanFile != null && cleanFile.exists()) {
            System.out.println("✅ SUCCESS: Clean copy saved at: " + cleanFile.getAbsolutePath());
        } else {
            System.out.println("❌ FAILED: Cleaning protocol failed.");
        }
    }
}