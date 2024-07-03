package com.github.idelstak.metadata.service;

import com.github.idelstak.metadata.model.*;
import javafx.concurrent.*;
import org.jaudiotagger.audio.*;
import org.slf4j.*;

import java.io.*;
import java.util.*;

import static javafx.application.Platform.*;

public class AudioFileResolveService extends Service<List<TaggedAudioFile>> {

    private static final Logger LOG = LoggerFactory.getLogger(AudioFileResolveService.class);
    private final List<File> files;

    public AudioFileResolveService(List<File> files) {
        this.files = files;
    }

    @Override
    protected Task<List<TaggedAudioFile>> createTask() {
        return new Task<>() {
            @Override
            protected List<TaggedAudioFile> call() {
                List<TaggedAudioFile> taggedAudioFiles = new ArrayList<>();
                int filesCount = files.size();

                // Initial progress update
                runLater(() -> {
                    updateMessage("Starting to load %d files...".formatted(filesCount));
                    updateProgress(0, filesCount);
                });

                for (int i = 0; i < filesCount; i++) {
                    if (isCancelled()) {
                        runLater(() -> updateMessage("Task cancelled"));
                        break;
                    }
                    File file = files.get(i);
                    runLater(() -> updateMessage("Loading " + file.getName()));

                    try {
                        AudioFile audioFile = AudioFileIO.read(file);
                        TaggedAudioFile taggedAudioFile = new TaggedAudioFile(audioFile);
                        taggedAudioFiles.add(taggedAudioFile);
                        // Update the value with the latest resolved file
                        runLater(() -> updateValue(List.of(taggedAudioFile)));
                    } catch (Exception e) {
                        // Log and handle the error, but continue processing other mixed
                        LOG.error("Error processing file: {}", file.getName(), e);
                        runLater(() -> updateMessage("Error loading " + file.getName()));
                    }

                    // Update progress
                    int finalI = i + 1;
                    runLater(() -> updateProgress(finalI, filesCount));
                }

                // Final progress update
                runLater(() -> updateMessage("Loaded %d files.".formatted((int) getWorkDone())));
                return taggedAudioFiles;
            }
        };
    }
}