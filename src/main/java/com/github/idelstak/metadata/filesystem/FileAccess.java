package com.github.idelstak.metadata.filesystem;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class FileAccess {

    private final File directory;

    public FileAccess(File directory) {
        this.directory = directory;
    }

    public List<File> getAudioFiles() throws IOException {
        return findAllFilesRecursively(directory).stream().filter(FileAccess::isAudioFile).toList();
    }

    public static List<File> findAllFilesRecursively(File directory) throws IOException {
        try (Stream<Path> paths = Files.walk(directory.toPath())) {
            return paths.toList().stream().filter(Files::isRegularFile).map(Path::toFile).toList();
        }
    }

    public static boolean isAudioFile(File file) {
        return file.getName().toLowerCase().endsWith(".mp3");
    }

    static void deleteDirectoryRecursively(File directory) throws IOException {
        deleteDirectoryRecursively(directory.toPath());
    }

    private static void deleteDirectoryRecursively(Path directory) throws IOException {
        if (Files.exists(directory)) {
            try (Stream<Path> visited = Files.walk(directory)) {
                // Sort entries in reverse order - delete files first
                Comparator<Path> reverseOrder = Comparator.reverseOrder();
                // Skip the directory itself (initially)
                Predicate<Path> ifNotDirectoryItself = path -> path != directory;
                // Delete all files contained in the directory
                for (Path path : visited.sorted(reverseOrder).filter(ifNotDirectoryItself).toList()) {
                    Files.delete(path);
                }
            } catch (IOException e) {
                throw new IOException("Failed to delete directory: " + directory, e);
            } finally {
                // Delete the directory itself (after it's empty)
                Files.delete(directory);
            }
        }
    }

//    private void findAudioFilesIn(File directory, List<File> audioFiles) {
//        File[] files = directory.listFiles();
//        if (files != null) {
//            for (File file : files) {
//                if (file.isFile() && isAudioFile(file)) {
//                    audioFiles.add(file);
//                } else if (file.isDirectory()) {
//                    // Recursively search in subdirectories
//                    findAudioFilesIn(file, audioFiles);
//                }
//            }
//        }
//    }
}