package com.example.hellofx.scanner;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.ooxml.POIXMLProperties;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class DocxScanner {
    public static List<String> scanDOCX(File docxFile) {
        List<String> exposedData = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(docxFile);
             XWPFDocument document = new XWPFDocument(fis)) {

            POIXMLProperties.CoreProperties props = document.getProperties().getCoreProperties();

            // 1. Original Author
            if (props.getCreator() != null) {
                exposedData.add("[DOCX] Original Author: " + props.getCreator());
            }

            // 2. Last Person to Touch the File
            if (props.getModified() != null) {
                exposedData.add("[DOCX] Last Modified By: " + props.getModified());
            }

            // 3. Revision Number (Save Count)
            if (props.getRevision() != null) {
                exposedData.add("[DOCX] Revision Number: " + props.getRevision());
            }

            // 4. Document Category (Internal metadata)
            if (props.getCategory() != null) {
                exposedData.add("[DOCX] Category: " + props.getCategory());
            }

            // 5. Creation Timestamp
            if (props.getCreated() != null) {
                exposedData.add("[DOCX] Date Created: " + props.getCreated());
            }

            // 6. Last Modification Timestamp
            if (props.getModified() != null) {
                exposedData.add("[DOCX] Date Last Modified: " + props.getModified().toString());
            }

        } catch (Exception e) {
            exposedData.add("Error scanning DOCX: " + e.getMessage());
        }

        return exposedData;
    }
}
