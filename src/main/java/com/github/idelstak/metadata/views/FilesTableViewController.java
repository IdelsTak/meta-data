package com.github.idelstak.metadata.views;

import com.github.idelstak.metadata.filesystem.*;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.concurrent.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import org.slf4j.*;

import java.io.*;
import java.util.*;

import static javafx.application.Platform.*;

public class FilesTableViewController extends FxmlController {

    private static final Logger LOG = LoggerFactory.getLogger(FilesTableViewController.class);
    private final ObjectProperty<File> rootDirectory;
    private final ObservableList<TaggedAudioFile> taggedAudioFiles;
    private final IntegerProperty audioFilesCount;
    @FXML
    private TableView<TaggedAudioFile> filesTableView;
    @FXML
    private TableColumn<TaggedAudioFile, String> artistColumn;
    @FXML
    private TableColumn<TaggedAudioFile, String> yearColumn;
    @FXML
    private TableColumn<TaggedAudioFile, String> titleColumn;
    @FXML
    private TableColumn<TaggedAudioFile, String> trackColumn;
    @FXML
    private TableColumn<TaggedAudioFile, String> albumColumn;

    public FilesTableViewController() {
        rootDirectory = new SimpleObjectProperty<>();
        taggedAudioFiles = FXCollections.observableArrayList();
        audioFilesCount = new SimpleIntegerProperty();
    }

    @Override
    protected void initialize() {
        rootDirectory.addListener((_, _, directory) -> {
            LOG.debug("root directory set to: {}", directory);

            if (directory == null) {
                return;
            }

            List<File> filesTmp = new ArrayList<>();
            try {
                filesTmp = new FileAccess(directory).getAudioFiles();
            } catch (IOException e) {
                LOG.error("", e);
            }

            LOG.info("Mapping {} audio files to tagged audio files...", filesTmp.size());

            List<File> files = new ArrayList<>(filesTmp);
            runLater(() -> {
                audioFilesCount.set(files.size());
                taggedAudioFiles.clear();
            });

            Service<List<TaggedAudioFile>> resolveService = new AudioFileResolveService(files);
            resolveService.messageProperty().addListener((_, _, message) -> {
                if (message == null) {
                    return;
                }
                LOG.info(message);
            });
            resolveService.progressProperty().addListener((_, _, progress) -> {
                LOG.info("progress: {}", progress);
            });
            resolveService.valueProperty().addListener((_, _, latestTaggedAudioFiles) -> {
                if (latestTaggedAudioFiles != null && latestTaggedAudioFiles.size() == 1) {
                    TaggedAudioFile latestResolvedFile = latestTaggedAudioFiles.getFirst();
                    LOG.info("Latest resolved file: {}", latestResolvedFile);
                    runLater(() -> taggedAudioFiles.add(latestResolvedFile));
                }
            });
            resolveService.setOnFailed(event -> {
                LOG.error("ResolveService failed to map tagged audio files", event.getSource().getException());
            });
            resolveService.setOnSucceeded(event -> {
                @SuppressWarnings("unchecked")
                List<TaggedAudioFile> tagged = (List<TaggedAudioFile>) event.getSource().getValue();
                LOG.info("ResolveService successfully mapped {} tagged files", tagged.size());
            });

            resolveService.start();
        });

        trackColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().track()));
        artistColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().artist()));
        titleColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().title()));
        albumColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().album()));
        yearColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().year()));

        filesTableView.setItems(taggedAudioFiles);
    }

    void setDirectory(File rootDirectory) {
        this.rootDirectory.set(rootDirectory);
    }

    @SuppressWarnings("unchecked")
    <T> TableView<T> getFilesView() {
        return (TableView<T>) filesTableView;
    }

    int audioFilesCount() {
        return audioFilesCount.get();
    }
}