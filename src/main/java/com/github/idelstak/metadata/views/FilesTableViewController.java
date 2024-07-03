package com.github.idelstak.metadata.views;

import com.github.idelstak.metadata.components.*;
import com.github.idelstak.metadata.filesystem.*;
import com.github.idelstak.metadata.model.*;
import com.github.idelstak.metadata.service.*;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.collections.ListChangeListener.*;
import javafx.concurrent.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.control.TableView.*;
import org.slf4j.*;

import java.io.*;
import java.util.*;

import static com.github.idelstak.metadata.components.Fxml.*;
import static javafx.application.Platform.*;

public class FilesTableViewController extends FxmlController {

    private static final Logger LOG = LoggerFactory.getLogger(FilesTableViewController.class);
    private final ObjectProperty<File> rootDirectory;
    private final ObservableList<TaggedAudioFile> taggedAudioFiles;
    private final IntegerProperty audioFilesCount;
    private final BooleanProperty fileSelected;
    @FXML
    private TableView<TaggedAudioFile> filesTableView;
    @FXML
    private TableColumn<TaggedAudioFile, String> artistColumn;
    @FXML
    private TableColumn<TaggedAudioFile, String> titleColumn;
    @FXML
    private TableColumn<TaggedAudioFile, String> albumColumn;
    private BooleanProperty cancelFileLoad;
    private DoubleProperty filesLoadedProperty;
    private DoubleProperty filesLoadProgressProperty;
    private StringProperty filesLoadMessageProperty;
    private DoubleProperty totalFilesLoadingProperty;
    private BooleanProperty filesLoadingProperty;

    public FilesTableViewController() {
        rootDirectory = new SimpleObjectProperty<>();
        taggedAudioFiles = FXCollections.observableArrayList();
        audioFilesCount = new SimpleIntegerProperty();
        fileSelected = new SimpleBooleanProperty();
        filesLoadMessageProperty = new SimpleStringProperty();
    }

    @Override
    protected void initialize() {
        TableViewSelectionModel<TaggedAudioFile> filesSelection = filesTableView.getSelectionModel();
        fileSelected.bind(filesSelection.selectedItemProperty().map(Objects::nonNull));
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

            List<File> files = new ArrayList<>(filesTmp);
            runLater(() -> {
                audioFilesCount.set(files.size());
                taggedAudioFiles.clear();
            });

            Service<List<TaggedAudioFile>> resolveService = new AudioFileResolveService(files);
            resolveService.messageProperty().addListener((_, _, message) -> LOG.debug(message));
            resolveService.progressProperty().addListener((_, _, progress) -> LOG.debug("progress: {}", progress));
            resolveService.valueProperty().addListener((_, _, latestTaggedAudioFiles) -> {
                if (latestTaggedAudioFiles != null && latestTaggedAudioFiles.size() == 1) {
                    TaggedAudioFile latestResolvedFile = latestTaggedAudioFiles.getFirst();
                    LOG.debug("Latest resolved file: {}", latestResolvedFile);
                    runLater(() -> taggedAudioFiles.add(latestResolvedFile));
                }
            });
            resolveService.setOnFailed(event -> {
                LOG.error("ResolveService failed to map tagged audio mixed", event.getSource().getException());
            });
            resolveService.setOnSucceeded(event -> {
                @SuppressWarnings("unchecked")
                List<TaggedAudioFile> tagged = (List<TaggedAudioFile>) event.getSource().getValue();
                LOG.debug("ResolveService successfully mapped {} tagged mixed", tagged.size());
            });

            if (cancelFileLoad != null) {
                cancelFileLoad.addListener((_, _, cancel) -> {
                    if (cancel != null && cancel) {
                        resolveService.cancel();
                    }
                });
            }
            if (filesLoadedProperty != null) {
                filesLoadedProperty.bind(resolveService.workDoneProperty());
            }
            if (filesLoadProgressProperty != null) {
                filesLoadProgressProperty.bind(resolveService.progressProperty());
            }
            if (filesLoadMessageProperty != null) {
                filesLoadMessageProperty.bind(resolveService.messageProperty());
            }
            if (totalFilesLoadingProperty != null) {
                totalFilesLoadingProperty.bind(resolveService.totalWorkProperty());
            }
            if (filesLoadingProperty != null) {
                filesLoadingProperty.bind(resolveService.runningProperty());
            }
            resolveService.start();
        });

        artistColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().artist()));
        titleColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().title()));
        albumColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().album()));

        filesTableView.setItems(taggedAudioFiles);

        ReadOnlyObjectProperty<TaggedAudioFile> itemProperty = filesSelection.selectedItemProperty();
        itemProperty.addListener((_, _, taggedAudioFile) -> {
            runLater(() -> {
                try {
                    SongInfoViewController controller = (SongInfoViewController) SONG_INFO_VIEW.controller();
                    controller.setTaggedAudioFile(taggedAudioFile);
                } catch (IOException e) {
                    LOG.error("", e);
                }
            });
        });

        filesTableView.getItems().addListener((Change<? extends TaggedAudioFile> change) -> {
            if (change.next() && change.wasAdded()) {
                runLater(() -> {
                    if (filesSelection.getSelectedItem() == null) {
                        filesSelection.selectFirst();
                    }
                });
            }
        });
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

    void updateView(TaggedAudioFile taggedAudioFile) {
        taggedAudioFiles.stream()
                        .filter(file -> Objects.equals(file.fileName(), taggedAudioFile.fileName()))
                        .map(taggedAudioFiles::indexOf)
                        .findFirst()
                        .ifPresent(index -> runLater(() -> {
                            taggedAudioFiles.set(index, taggedAudioFile);
                            TableViewSelectionModel<TaggedAudioFile> selection = filesTableView.getSelectionModel();
                            selection.clearSelection();
                            new Thread(() -> {
                                try {
                                    Thread.sleep(500L);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                                selection.select(taggedAudioFile);
                            }).start();
                        }));
    }

    BooleanProperty fileSelected() {
        return fileSelected;
    }

    void setFileLoadCancelProperty(BooleanProperty cancelFileLoad) {
        this.cancelFileLoad = cancelFileLoad;
    }

    void setFilesLoadedProperty(DoubleProperty filesLoadedProperty) {
        this.filesLoadedProperty = filesLoadedProperty;
    }

    void setFilesLoadProgressProperty(DoubleProperty filesLoadProgressProperty) {
        this.filesLoadProgressProperty = filesLoadProgressProperty;
    }

    void setFilesLoadMessageProperty(StringProperty filesLoadMessageProperty) {
        this.filesLoadMessageProperty = filesLoadMessageProperty;
    }

    void setTotalFilesLoadingProperty(DoubleProperty totalFilesLoadingProperty) {
        this.totalFilesLoadingProperty = totalFilesLoadingProperty;
    }

    void setFilesLoadingProperty(BooleanProperty filesLoadingProperty) {
        this.filesLoadingProperty = filesLoadingProperty;
    }
}