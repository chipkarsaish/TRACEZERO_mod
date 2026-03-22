package com.example.hellofx.utils;

import java.io.File;

public class FileTypeDetector {

    // This defines the exact categories our engine understands
    public enum FileType {
        IMAGE,
        PDF,
        DOCX,
        ZIP,   // 🚀 Added ZIP support
        UNSUPPORTED
    }

    /**
     * Looks at a file and tells the engine exactly how to route it.
     */
    public static FileType detect(File file) {
        if (file == null || !file.exists()) {
            return FileType.UNSUPPORTED;
        }

        String fileName = file.getName().toLowerCase();

        // 1. Check for Images
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")) {
            return FileType.IMAGE;
        }
        // 2. Check for PDFs
        else if (fileName.endsWith(".pdf")) {
            return FileType.PDF;
        }
        // 3. Check for Word Documents
        else if (fileName.endsWith(".docx") || fileName.endsWith(".doc")) {
            return FileType.DOCX;
        }
        // 4. Check for ZIP Archives
        else if (fileName.endsWith(".zip")) { // 🚀 Route for ZIP files
            return FileType.ZIP;
        }

        // If we don't know what it is, we reject it to keep the engine safe
        return FileType.UNSUPPORTED;
    }
}