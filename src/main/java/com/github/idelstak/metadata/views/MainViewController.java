package com.github.idelstak.metadata.views;

import com.github.idelstak.metadata.components.*;
import com.github.idelstak.metadata.model.*;
import javafx.beans.binding.*;
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
    @FXML
    private Label spacerLabel;
    @FXML
    private SplitPane mainSplitPane;
    @FXML
    private Button writeMetadataButton;
    @FXML
    private Button fetchMetadataButton;

    @Override
    protected void initialize() throws IOException {
        spacerLabel.skinProperty().addListener(_ -> runLater(() -> {
            try {
                loadViews();
                FilesTableViewController controller = (FilesTableViewController) FILES_TABLE_VIEW.controller();
                BooleanBinding noFileSelected = controller.fileSelected().not();
                writeMetadataButton.disableProperty().bind(noFileSelected);
                writeMetadataButton.disableProperty().bind(noFileSelected);
                fetchMetadataButton.disableProperty().bind(noFileSelected);
            } catch (IOException e) {
                LOG.error("", e);
            }
        }));
        HBox.setHgrow(spacerLabel, Priority.ALWAYS);
        mainSplitPane.heightProperty().addListener((_, _, _) -> runLater(this::updateDividerPosition));
    }

    private void loadViews() throws IOException {
        mainSplitPane.getItems().addAll(filesTableView(), songInfoPane());
        updateDividerPosition();
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
            FilesTableViewController controller = (FilesTableViewController) FILES_TABLE_VIEW.controller();
            controller.setDirectory(directory);
        }
    }
}