/*
 * MIT License
 *
 * Copyright (c) 2024 Hiram K
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sub-license, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.idelstak.metadata.views;

import com.github.idelstak.metadata.components.Alert;
import com.github.idelstak.metadata.components.*;
import com.github.idelstak.metadata.model.*;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.beans.value.*;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.util.*;
import org.slf4j.*;

import java.io.*;
import java.util.*;

import static com.github.idelstak.metadata.components.Fxml.*;
import static javafx.application.Platform.*;
import static javafx.scene.control.ButtonType.*;

public class MainViewController extends FxmlController {

    private static final Logger LOG = LoggerFactory.getLogger(MainViewController.class);
    private final BooleanProperty cancelFileLoad;
    private final DoubleProperty filesLoadedProperty;
    private final DoubleProperty filesLoadProgressProperty;
    private final StringProperty filesLoadMessageProperty;
    private final DoubleProperty totalFilesLoadingProperty;
    private final BooleanProperty filesLoadingProperty;
    @FXML
    private SplitPane mainSplitPane;
    @FXML
    private Button writeMetadataButton;
    @FXML
    private Button fetchMetadataButton;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label progressLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Button cancelFileLoadButton;
    @FXML
    private Label directoryPathLabel;
    @FXML
    private Label directoryNameLabel;
    @FXML
    private VBox directoryInfoBox;
    @FXML
    private Button reloadDirectoryButton;
    @FXML
    private BorderPane welcomeScreen;
    @FXML
    private BorderPane mainViewPane;
    @FXML
    private ProgressIndicator firstLoadprogressIndicator;

    public MainViewController() {
        cancelFileLoad = new SimpleBooleanProperty();
        filesLoadedProperty = new SimpleDoubleProperty();
        filesLoadProgressProperty = new SimpleDoubleProperty();
        filesLoadMessageProperty = new SimpleStringProperty();
        totalFilesLoadingProperty = new SimpleDoubleProperty();
        filesLoadingProperty = new SimpleBooleanProperty();
    }

    @Override
    protected void initialize() throws IOException {
        writeMetadataButton.skinProperty().addListener(_ -> runLater(() -> {
            try {
                loadViews();
                FilesTableViewController controller = (FilesTableViewController) FILES_TABLE_VIEW.controller();
                BooleanBinding noFileSelected = controller.fileSelected().not();
                writeMetadataButton.disableProperty().bind(noFileSelected);
                writeMetadataButton.disableProperty().bind(noFileSelected);
                fetchMetadataButton.disableProperty().bind(noFileSelected);
                firstLoadprogressIndicator.progressProperty()
                                          .bind(filesLoadingProperty.map(loading -> loading ? -1 : 1));
                Pane parent = ((Pane) firstLoadprogressIndicator.getParent());
                ObservableValue<Double> mappedIndicatorWidth = parent.widthProperty()
                                                                     .map(Number::doubleValue)
                                                                     .map(width -> width * 0.5f);
                firstLoadprogressIndicator.prefWidthProperty().bind(mappedIndicatorWidth);
                ReadOnlyDoubleProperty indicatorWidth = firstLoadprogressIndicator.widthProperty();
                firstLoadprogressIndicator.prefHeightProperty().bind(indicatorWidth);
                firstLoadprogressIndicator.visibleProperty().bind(noFileSelected);
                mainSplitPane.visibleProperty().bind(noFileSelected.not());
                ObjectProperty<File> loadedDirectory = controller.loadedDirectory();
                BooleanBinding noDirectoryLoaded = loadedDirectory.isNull();
                welcomeScreen.visibleProperty().bind(noDirectoryLoaded);
                mainViewPane.visibleProperty().bind(noDirectoryLoaded.not());
                reloadDirectoryButton.disableProperty().bind(noDirectoryLoaded);
                reloadDirectoryButton.addEventFilter(ActionEvent.ACTION, event -> {
                    try {
                        prepareDirectoryViewing(loadedDirectory.get());
                    } catch (IOException e) {
                        Window owner = reloadDirectoryButton.getScene().getWindow();
                        Alert.ERROR.show(owner, "", e);
                        event.consume();
                    }
                });
                directoryNameLabel.textProperty().bind(loadedDirectory.map(File::getName));
                directoryPathLabel.textProperty().bind(loadedDirectory.map(File::getPath));
            } catch (IOException e) {
                LOG.error("", e);
            }
        }));
        HBox.setHgrow(directoryInfoBox, Priority.ALWAYS);
        mainSplitPane.heightProperty().addListener((_, _, _) -> runLater(this::updateDividerPosition));

        progressBar.progressProperty().bind(filesLoadProgressProperty);
        progressLabel.textProperty().bind(filesLoadedProperty.map(Number::intValue).map(done -> {
            return "%d/%d loaded".formatted(done, (int) totalFilesLoadingProperty.get());
        }));
        statusLabel.textProperty().bind(filesLoadMessageProperty);
        progressBar.visibleProperty().bind(filesLoadingProperty);
        progressLabel.visibleProperty().bind(filesLoadingProperty);
        cancelFileLoadButton.visibleProperty().bind(filesLoadingProperty);
        filesLoadingProperty.addListener((_, _, loading) -> {
            if (loading != null) {
                if (!loading) {
                    new Thread(() -> {
                        try {
                            Thread.sleep(5000L);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        runLater(() -> statusLabel.setVisible(false));
                    }).start();
                } else {
                    runLater(() -> statusLabel.setVisible(true));
                }
            }
        });
    }

    private void loadViews() throws IOException {
        mainSplitPane.getItems().addAll(filesTableView(), songInfoPane());
        updateDividerPosition();
    }

    private void prepareDirectoryViewing(File directory) throws IOException {
        FilesTableViewController controller = (FilesTableViewController) FILES_TABLE_VIEW.controller();
        controller.setFileLoadCancelProperty(cancelFileLoad);
        controller.setFilesLoadedProperty(filesLoadedProperty);
        controller.setFilesLoadProgressProperty(filesLoadProgressProperty);
        controller.setFilesLoadMessageProperty(filesLoadMessageProperty);
        controller.setTotalFilesLoadingProperty(totalFilesLoadingProperty);
        controller.setFilesLoadingProperty(filesLoadingProperty);
        controller.setDirectory(null);
        controller.setDirectory(directory);
    }

    private void updateDividerPosition() {
        mainSplitPane.setDividerPosition(0, 0.9);
    }

    private Node filesTableView() throws IOException {
        return FILES_TABLE_VIEW.root();
    }

    private Node songInfoPane() throws IOException {
        return SONG_INFO_VIEW.root();
    }

    @FXML
    private void reloadDirectory(ActionEvent actionEvent) {
    }

    @FXML
    private void cancelFileLoading(ActionEvent actionEvent) {
        cancelFileLoad.setValue(null);
        cancelFileLoad.set(true);
        actionEvent.consume();
    }

    @FXML
    private void writeMetadata(ActionEvent actionEvent) throws IOException {
        SongInfoViewController controller = (SongInfoViewController) SONG_INFO_VIEW.controller();
        TaggedAudioFile updatedTaggedAudioFile = controller.updateTaggedAudioFile();
        FilesTableViewController filesController = (FilesTableViewController) FILES_TABLE_VIEW.controller();
        filesController.updateView(updatedTaggedAudioFile);
        actionEvent.consume();
    }

    @FXML
    private void fetchMetadata(ActionEvent actionEvent) throws IOException {
        Window owner = ((Node) actionEvent.getSource()).getScene().getWindow();
        SongInfoViewController controller = (SongInfoViewController) SONG_INFO_VIEW.controller();
        Pair<String, String> titlePair = new QueryPair("TITLE", controller.title());
        Pair<String, String> artistPair = new QueryPair("ARTIST", controller.artist());
        Pair<String, String> albumPair = new QueryPair("ALBUM", controller.album());
        MetadataQuery query = new MetadataQuery(titlePair, artistPair, albumPair);
        TaggedAudioFile taggedAudioFile = controller.taggedAudioFile();
        Optional<ButtonType> selection = new MetadataFetchDialog(owner, query, taggedAudioFile).showAndWait();
        selection.stream().filter(type -> type == OK).findFirst().ifPresent(_ -> {
            FilesTableViewController filesController = null;
            try {
                filesController = (FilesTableViewController) FILES_TABLE_VIEW.controller();
            } catch (IOException e) {
                LOG.error("", e);
            }

            if (filesController == null) {
                return;
            }

            filesController.updateView(taggedAudioFile);
        });
    }

    @FXML
    private void openDirectory(ActionEvent actionEvent) throws IOException {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Directory");

        Window owner = ((Node) actionEvent.getSource()).getScene().getWindow();
        File directory = directoryChooser.showDialog(owner);
        LOG.info("Selected directory: {}", directory);

        if (directory != null) {
            prepareDirectoryViewing(directory);
        }
    }
}