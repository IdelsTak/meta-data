package com.github.idelstak.metadata.views;

import com.github.idelstak.metadata.filesystem.*;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.concurrent.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.control.TableView.*;
import org.slf4j.*;

import java.io.*;
import java.util.*;

import static com.github.idelstak.metadata.views.Fxml.*;
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
        fileSelected = new SimpleBooleanProperty();
    }

    @Override
    protected void initialize() {
        fileSelected.bind(filesTableView.getSelectionModel().selectedItemProperty().map(Objects::nonNull));
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

            LOG.debug("Mapping {} audio mixed to tagged audio mixed...", filesTmp.size());

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

            resolveService.start();
        });

        trackColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().track()));
        artistColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().artist()));
        titleColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().title()));
        albumColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().album()));
        yearColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().year()));

        filesTableView.setItems(taggedAudioFiles);

        TableViewSelectionModel<TaggedAudioFile> selectionModel = filesTableView.getSelectionModel();
        ReadOnlyObjectProperty<TaggedAudioFile> itemProperty = selectionModel.selectedItemProperty();
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
}