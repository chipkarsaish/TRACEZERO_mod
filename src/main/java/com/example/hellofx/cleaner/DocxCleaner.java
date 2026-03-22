package com.example.hellofx.cleaner;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.ooxml.POIXMLProperties;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class DocxCleaner {
    public static File cleanDOCX(File originalFile) {
        try (FileInputStream fis = new FileInputStream(originalFile);
             XWPFDocument document = new XWPFDocument(fis)) {

            POIXMLProperties.CoreProperties props = document.getProperties().getCoreProperties();

            // 1. Wipe text-based fields
            props.setCreator(null);
            props.setCategory(null);
            props.setRevision(null);

            // 2. Wipe date-based fields (Explicitly telling Java these are Dates)
            props.getUnderlyingProperties().setCreatedProperty(java.util.Optional.empty());

            // 3. Wipe the "Modified" fields (Both the name and the date)
            props.setModified((java.lang.String) null);
            props.getUnderlyingProperties().setLastModifiedByProperty((String) null);

            String cleanPath = getCleanPath(originalFile);
            File cleanCopy = new File(cleanPath);

            try (FileOutputStream fos = new FileOutputStream(cleanCopy)) {
                document.write(fos);
            }

            return cleanCopy;
        } catch (Exception e) {
            System.out.println("DOCX Clean Error: " + e.getMessage());
            return null;
        }
    }

    private static String getCleanPath(File file) {
        String path = file.getAbsolutePath();
        int dotIndex = path.lastIndexOf(".");
        return path.substring(0, dotIndex) + "_clean" + path.substring(dotIndex);
    }
}
