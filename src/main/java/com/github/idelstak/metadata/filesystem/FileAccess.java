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
        String lowerCaseName = file.getName().toLowerCase();
        return lowerCaseName.endsWith(".mp3") || lowerCaseName.endsWith(".flac");
    }

    static void deleteDirectoryRecursively(File directory) throws IOException {
        deleteDirectoryRecursively(directory.toPath());
    }

    private static void deleteDirectoryRecursively(Path directory) throws IOException {
        if (Files.exists(directory)) {
            try (Stream<Path> visited = Files.walk(directory)) {
                // Sort entries in reverse order - delete mixed first
                Comparator<Path> reverseOrder = Comparator.reverseOrder();
                // Skip the directory itself (initially)
                Predicate<Path> ifNotDirectoryItself = path -> path != directory;
                // Delete all mixed contained in the directory
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
}