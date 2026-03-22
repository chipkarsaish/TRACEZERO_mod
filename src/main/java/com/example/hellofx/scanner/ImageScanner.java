package com.example.hellofx.scanner;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageScanner {

    public static List<String> scanImage(File imageFile) {
        List<String> dataList = new ArrayList<>();
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(imageFile);

            for (Directory directory : metadata.getDirectories()) {
                String dirName = directory.getName();

                // 🚀 THE FIX: Ignore structural and generic file system directories
                if (dirName.contains("JPEG") || dirName.contains("JFIF") ||
                        dirName.contains("Huffman") || dirName.equalsIgnoreCase("File Type") ||
                        dirName.equalsIgnoreCase("File")) {
                    continue; // Skip to the next directory
                }

                for (Tag tag : directory.getTags()) {
                    dataList.add("[" + dirName + "] " + tag.getTagName() + ": " + tag.getDescription());
                }
            }
        } catch (Exception e) {
            dataList.add("Error reading image metadata: " + e.getMessage());
        }
        return dataList;
    }
}