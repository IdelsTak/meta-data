package com.github.idelstak.metadata.filesystem;

import java.io.*;
import java.util.*;

public class FileAccess {

    private final File directory;

    public FileAccess(File directory) {
        this.directory = directory;
    }

    List<File> getAudioFiles() {
        List<File> audioFiles = new ArrayList<>();
        findAudioFilesIn(directory, audioFiles);
        return audioFiles;
    }

    static boolean isAudioFile(File file) {
        return file.getName().toLowerCase().endsWith(".mp3");
    }

    private void findAudioFilesIn(File directory, List<File> audioFiles) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && isAudioFile(file)) {
                    audioFiles.add(file);
                } else if (file.isDirectory()) {
                    // Recursively search in subdirectories
                    findAudioFilesIn(file, audioFiles);
                }
            }
        }
    }
}