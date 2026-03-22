package com.example.hellofx.scanner;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PdfScanner {
    public static List<String> scanPDF(File pdfFile) {
        List<String> exposedData = new ArrayList<>();
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDDocumentInformation info = document.getDocumentInformation();
            if (info.getAuthor() != null) exposedData.add("[PDF] Author: " + info.getAuthor());
            if (info.getCreator() != null) exposedData.add("[PDF] Creator Software: " + info.getCreator());
            if (info.getProducer() != null) exposedData.add("[PDF] Producer: " + info.getProducer());
            if (info.getTitle() != null) exposedData.add("[PDF] Title: " + info.getTitle());

            Calendar creationDate = info.getCreationDate();
            if (creationDate != null) exposedData.add("[PDF] Creation Date: " + creationDate.getTime());
        } catch (Exception e) {
            exposedData.add("Error scanning PDF: " + e.getMessage());
        }
        return exposedData;
    }
}
