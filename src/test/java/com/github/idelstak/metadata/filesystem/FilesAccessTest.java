package com.github.idelstak.metadata.filesystem;

import org.junit.jupiter.api.*;
import org.slf4j.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import static org.assertj.core.api.Assertions.*;

public class FilesAccessTest {

    private static final Logger LOG = LoggerFactory.getLogger(FilesAccessTest.class);
    private static final String SAMPLE_FILES_PATH = "/sample/files/";
    private static final String NESTED_FILES_PATH = "nested";
    private static URL sampleFilesUrl;

    protected static URL sampleFilesUrl() {
        return sampleFilesUrl;
    }

    protected static URL mp3sDir() {
        return FilesAccessTest.class.getResource("/mp3s");
    }

    @BeforeAll
    static void setUp() throws IOException, URISyntaxException {
        cleanupSampleFiles();
        // Distribute sample files in a nested directory structure
        distributeAndNestFiles();
        sampleFilesUrl = FilesAccessTest.class.getResource(SAMPLE_FILES_PATH + NESTED_FILES_PATH);
    }

    @AfterAll
    static void tearDown() throws IOException {
        cleanupSampleFiles();
    }

    private static void cleanupSampleFiles() throws IOException {
        if (sampleFilesUrl != null) {
            File nestedDirectory = new File(sampleFilesUrl.getFile());
            // Recursively deletes all files and subdirectories within the specified directory
            if (nestedDirectory.exists()) {
                FileAccess.deleteDirectoryRecursively(nestedDirectory);
            }
        }
    }

    @Test
    void shouldRetrieveAllAudioFilesFromNestedStructure() throws IOException {
        assertThat(sampleFilesUrl).isNotNull();
        File directory = new File(sampleFilesUrl.getFile());
        File[] files = FileAccess.findAllFilesRecursively(directory).toArray(File[]::new);
        assertThat(files).isNotNull();
        FileAccess fileAccess = new FileAccess(directory);

        // Assert that the audio files in FileAccess match those distributed and nested in the sample files
        List<File> audioFilesFromDistributed = Arrays.stream(files).filter(FileAccess::isAudioFile).toList();
        assertThat(fileAccess.getAudioFiles()).containsExactlyInAnyOrderElementsOf(audioFilesFromDistributed);
    }

    private static void distributeAndNestFiles() throws IOException, URISyntaxException {
        List<File> files = findSampleFiles();
        // Ensure resources directory exists
        URL resourcesRoot = FilesAccessTest.class.getResource(SAMPLE_FILES_PATH);
        if (resourcesRoot == null) {
            throw new IOException("Test resources directory not found: " + SAMPLE_FILES_PATH);
        }

        File rootDir = new File(resourcesRoot.getFile(), NESTED_FILES_PATH);
        // Create directories if they don't exist
        Files.createDirectories(rootDir.toPath());
        distributeFiles(rootDir, files);
    }

    private static List<File> findSampleFiles() throws IOException, URISyntaxException {
        List<File> files = new ArrayList<>();
        // Locate sample files from resources directory
        URL resourcesRoot = FilesAccessTest.class.getResource(SAMPLE_FILES_PATH);
        if (resourcesRoot != null) {
            try (Stream<Path> paths = Files.walk(Paths.get(resourcesRoot.toURI()))) {
                files.addAll(paths.map(Path::toFile).toList());
            }
        }
        return files;
    }

    private static void distributeFiles(File rootDir, List<File> files) throws IOException {
        // Shuffle files randomly
        Collections.shuffle(files);
        // Distribute each file into the root directory
        for (File file : files) {
            // 50% chance of creating subdirectory
            nestFile(rootDir, file, 0.5);
        }
    }

    private static void nestFile(File currentDir, File file, double nestedProbability) throws IOException {
        if (Math.random() < nestedProbability) {
            // Create a subdirectory with unique suffix
            String suffix = String.valueOf(System.currentTimeMillis());
            File subDir = new File(currentDir, "subDir_" + suffix);
            Files.createDirectories(subDir.toPath());
            // Recursively nest file within the subdirectory
            nestFile(subDir, file, nestedProbability);
        } else {
            // Copy the file into the current directory
            Path targetPath = currentDir.toPath().resolve(file.getName());
            LOG.debug("Copying file to: {}", targetPath);
            Files.copy(file.toPath(), targetPath);
            LOG.debug("Copied {} to directory: {}", file.getName(), currentDir);
        }
    }
}