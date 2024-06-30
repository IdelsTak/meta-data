package com.github.idelstak.metadata.filesystem;

import org.junit.jupiter.api.*;
import org.slf4j.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import static com.github.idelstak.metadata.filesystem.SampleFile.*;
import static org.assertj.core.api.Assertions.*;

public class FilesAccessTest {

    private static final Logger LOG = LoggerFactory.getLogger(FilesAccessTest.class);
    private final File nestedFilesDir = new File(MIXED.url().getFile(), "nested");
    private URL nestedFilesUrl;

    @BeforeEach
    void setUp() throws IOException, URISyntaxException {
        cleanupNestedFiles();
        // Distribute sample mixed in a nested directory structure
        distributeAndNestFiles();
        nestedFilesUrl = nestedFilesDir.toURI().toURL();
    }

    @AfterEach
    void tearDown() throws IOException {
        cleanupNestedFiles();
    }

    private void cleanupNestedFiles() throws IOException {
        if (nestedFilesUrl != null) {
            File nestedDirectory = new File(nestedFilesUrl.getFile());
            // Recursively deletes all mixed and subdirectories within the specified directory
            if (nestedDirectory.exists()) {
                FileAccess.deleteDirectoryRecursively(nestedDirectory);
            }
        }
    }

    @Test
    void retrieves_all_audio_files_from_nested_structure() throws IOException {
        assertThat(nestedFilesUrl).isNotNull();
        File directory = new File(nestedFilesUrl.getFile());
        File[] files = FileAccess.findAllFilesRecursively(directory).toArray(File[]::new);
        assertThat(files).isNotNull();
        FileAccess fileAccess = new FileAccess(directory);

        // Assert that the audio mixed in FileAccess match those distributed and nested in the sample mixed
        List<File> audioFilesFromDistributed = Arrays.stream(files).filter(FileAccess::isAudioFile).toList();
        assertThat(fileAccess.getAudioFiles()).containsExactlyInAnyOrderElementsOf(audioFilesFromDistributed);
    }

    private void distributeAndNestFiles() throws IOException, URISyntaxException {
        List<File> files = findSampleFiles();
        // Create directories if they don't exist
        Files.createDirectories(nestedFilesDir.toPath());
        distributeFiles(nestedFilesDir, files);
    }

    private static List<File> findSampleFiles() throws IOException, URISyntaxException {
        try (Stream<Path> paths = Files.walk(Paths.get(MIXED.url().toURI()))) {
            return new ArrayList<>(paths.map(Path::toFile).toList());
        }
    }

    private static void distributeFiles(File rootDir, List<File> files) throws IOException {
        // Shuffle mixed randomly
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