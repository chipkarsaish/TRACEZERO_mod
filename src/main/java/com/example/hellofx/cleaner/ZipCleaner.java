package com.example.hellofx.cleaner;

import java.io.*;
import java.nio.file.attribute.FileTime;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipCleaner {

    public static File cleanZIP(File originalZip) {
        String newName = originalZip.getName().replace(".zip", "_clean.zip");
        File cleanedZip = new File(originalZip.getParent(), newName);

        // Epoch time: Jan 1, 1970 (Wipes the real modification date)
        FileTime epoch = FileTime.fromMillis(0);

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(originalZip));
             ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(cleanedZip))) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();

                // 🚀 Skip OS junk files that leak system info
                if (name.contains(".DS_Store") || name.contains("__MACOSX") || name.contains("Thumbs.db")) {
                    zis.closeEntry();
                    continue;
                }

                // Create a clean entry and wipe its timestamp
                ZipEntry cleanEntry = new ZipEntry(name);
                cleanEntry.setLastModifiedTime(epoch);
                cleanEntry.setCreationTime(epoch);
                cleanEntry.setLastAccessTime(epoch);

                zos.putNextEntry(cleanEntry);

                // Copy the actual file data over
                byte[] buffer = new byte[1024];
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }

                zos.closeEntry();
                zis.closeEntry();
            }
            System.out.println("[✓] ZIP Sanitized: Timestamps wiped & OS trackers removed.");
            return cleanedZip;

        } catch (IOException e) {
            System.err.println("!!! ZIP Cleaning Error: " + e.getMessage());
            return null;
        }
    }
}
