package com.example.hellofx.cleaner;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import java.io.File;
import java.io.IOException;

public class PdfCleaner {
    public static File cleanPDF(File originalFile) {
        try (PDDocument document = Loader.loadPDF(originalFile)) {
            PDDocumentInformation info = document.getDocumentInformation();

            info.setAuthor(null);
            info.setCreator(null);
            info.setProducer(null);
            info.setTitle(null);
            info.setSubject(null);
            info.setKeywords(null);
            info.setCreationDate(null);
            info.setModificationDate(null);

            String cleanPath = getCleanPath(originalFile);
            File cleanCopy = new File(cleanPath);
            document.save(cleanCopy);

            return cleanCopy;
        } catch (IOException e) {
            System.out.println("PDF Clean Error: " + e.getMessage());
            return null;
        }
    }

    private static String getCleanPath(File file) {
        String path = file.getAbsolutePath();
        int dotIndex = path.lastIndexOf(".");
        return path.substring(0, dotIndex) + "_clean" + path.substring(dotIndex);
    }
}
