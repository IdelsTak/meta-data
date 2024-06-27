package com.github.idelstak.metadata.filesystem;

import org.junit.jupiter.api.*;
import org.slf4j.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import static org.assertj.core.api.Assertions.*;

class FileAccessTest {

    private static final Logger LOG = LoggerFactory.getLogger(FileAccessTest.class);
    private static String sampleFilesPath;
    private static String nestedFilesPath;

    @BeforeEach
    void setUp() {
        sampleFilesPath = "/sample/files/";
        nestedFilesPath = "nested";

        cleanupSampleFiles();
    }

    @AfterEach
    void tearDown() {
        cleanupSampleFiles();
    }

    @Test
    void shouldRetrieveAllAudioFilesFromNestedStructure() throws IOException, URISyntaxException {
        // First distribute sample files in a nested directory structure
        List<File> distributedFiles = distributeAndNestFiles();

        // Assert that the nested directory exists
        URL resource = getClass().getResource(sampleFilesPath + nestedFilesPath);
        assertThat(resource).isNotNull();

        FileAccess fileAccess = new FileAccess(new File(resource.getFile()));

        // Assert that the audio files in FileAccess match those distributed and nested in the sample files
        List<File> audioFilesFromDistributed = distributedFiles.stream().filter(FileAccess::isAudioFile).toList();
        assertThat(fileAccess.getAudioFiles()).containsExactlyInAnyOrderElementsOf(audioFilesFromDistributed);
    }

    private static void cleanupSampleFiles() {
        URL resource = FileAccessTest.class.getResource(sampleFilesPath + nestedFilesPath);
        if (resource != null) {
            File nestedDirectory = new File(resource.getFile());
            // Recursively deletes all files and subdirectories within the specified directory
            if (nestedDirectory.exists()) {
                deleteDirectory(nestedDirectory);
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }

    private static List<File> distributeAndNestFiles() throws IOException, URISyntaxException {
        List<File> files = findSampleFiles();
        // Ensure resources directory exists
        URL resourcesRoot = FileAccessTest.class.getResource(sampleFilesPath);
        if (resourcesRoot == null) {
            throw new IOException("Test resources directory not found: " + sampleFilesPath);
        }

        File rootDir = new File(resourcesRoot.getFile(), nestedFilesPath);
        // Create directories if they don't exist
        Files.createDirectories(rootDir.toPath());
        return distributeFiles(rootDir, files);
    }

    private static List<File> findSampleFiles() throws IOException, URISyntaxException {
        List<File> files = new ArrayList<>();
        // Locate sample files from resources directory
        URL resourcesRoot = FileAccessTest.class.getResource(sampleFilesPath);
        if (resourcesRoot != null) {
            try (Stream<Path> paths = Files.walk(Paths.get(resourcesRoot.toURI()))) {
                files.addAll(paths.map(Path::toFile).toList());
            }
        }
        return files;
    }

    private static List<File> distributeFiles(File rootDir, List<File> files) throws IOException {
        // Shuffle files randomly
        Collections.shuffle(files);
        List<File> distributedFiles = new ArrayList<>();

        // Distribute each file into the root directory
        for (File file : files) {
            // 50% chance of creating subdirectory
            distributedFiles.add(nestFile(rootDir, file, 0.5));
        }

        return distributedFiles;
    }

    private static File nestFile(File currentDir, File file, double nestedProbability) throws IOException {
        if (Math.random() < nestedProbability) {
            // Create a subdirectory with unique suffix
            String suffix = String.valueOf(System.currentTimeMillis());
            File subDir = new File(currentDir, "subDir_" + suffix);
            Files.createDirectories(subDir.toPath());
            // Recursively nest file within the subdirectory
            return nestFile(subDir, file, nestedProbability);
        } else {
            // Copy the file into the current directory
            Path targetPath = currentDir.toPath().resolve(file.getName());
            LOG.info("Copying file to: {}", targetPath);
            Files.copy(file.toPath(), targetPath);
            LOG.info("Copied {} to directory: {}", file.getName(), currentDir);
            return targetPath.toFile();
        }
    }
}