package org.shippin.util.io;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class TextFileHandler {

    public String readFrom(File file) {
        if (file == null || !file.exists() || !file.isFile() || !file.canRead()) {
            return "";
        }

        try {
            return Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Failed to read file: " + file.getAbsolutePath());
            e.printStackTrace();
            return "";
        }
    }


    public boolean writeTo(File file, String content) {
        if (file == null || content == null) {
            return false;
        }

        try {
            // create parent directories if they don't exist
            Files.createDirectories(file.toPath().getParent());
            Files.writeString(file.toPath(), content, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to write to file: " + file.getAbsolutePath());
            e.printStackTrace();
            return false;
        }
    }
}