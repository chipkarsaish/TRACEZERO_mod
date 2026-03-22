package com.example.hellofx.cleaner;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageCleaner {

    public static File cleanImage(File original) {
        try {
            // 1. Read the image into RAM (This drops all metadata headers automatically)
            BufferedImage bufferedImage = ImageIO.read(original);
            if (bufferedImage == null) return null;

            // 2. Prepare the output path
            String newName = original.getName().replaceFirst("[.][^.]+$", "") + "_clean.jpg";
            File cleanedFile = new File(original.getParent(), newName);

            // 3. Write ONLY the pixel data to the new file
            // ImageIO.write does NOT carry over EXIF/GPS/XMP data
            boolean success = ImageIO.write(bufferedImage, "jpg", cleanedFile);

            if (success && cleanedFile.exists()) {
                System.out.println("[✓] Hard-Cleaned: " + cleanedFile.getName());
                return cleanedFile;
            }
        } catch (IOException e) {
            System.err.println("Error cleaning image: " + e.getMessage());
        }
        return null;
    }
}