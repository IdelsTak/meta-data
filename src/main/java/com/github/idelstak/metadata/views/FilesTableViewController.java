package com.github.idelstak.metadata.views;

import com.github.idelstak.metadata.filesystem.*;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import org.slf4j.*;

import java.io.*;
import java.util.*;

import static javafx.application.Platform.*;

public class FilesTableViewController extends FxmlController {

    private static final Logger LOG = LoggerFactory.getLogger(FilesTableViewController.class);
    private final ObjectProperty<File> rootDirectory;
    private final ObservableList<File> audioFiles;
    @FXML
    private TableView<File> filesTableView;

    public FilesTableViewController() {
        rootDirectory = new SimpleObjectProperty<>();
        audioFiles = FXCollections.observableArrayList();
    }

    @Override
    protected void initialize() {
        rootDirectory.addListener((_, _, directory) -> {
            LOG.debug("root directory set to: {}", directory);

            if (directory == null) {
                return;
            }

            runLater(() -> {
                try {
                    List<File> audioFiles = new FileAccess(directory).getAudioFiles();
                    this.audioFiles.setAll(audioFiles);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });

        filesTableView.setItems(audioFiles);
    }

    void setDirectory(File rootDirectory) {
        this.rootDirectory.set(rootDirectory);
    }

    @SuppressWarnings("unchecked")
    <T> TableView<T> getFilesView() {
        return (TableView<T>) filesTableView;
    }
}