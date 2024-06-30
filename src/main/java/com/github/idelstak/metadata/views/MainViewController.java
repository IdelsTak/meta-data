package com.github.idelstak.metadata.views;

import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import org.slf4j.*;

import java.io.*;

import static com.github.idelstak.metadata.views.Fxml.*;
import static javafx.application.Platform.*;

public class MainViewController extends FxmlController {

    private static final Logger LOG = LoggerFactory.getLogger(MainViewController.class);
    @FXML
    private Label spacerLabel;
    @FXML
    private SplitPane mainSplitPane;

    @Override
    protected void initialize() {
        spacerLabel.skinProperty().addListener(_ -> runLater(() -> {
            try {
                loadViews();
            } catch (IOException e) {
                LOG.error("", e);
                throw new RuntimeException(e);
            }
        }));
        HBox.setHgrow(spacerLabel, Priority.ALWAYS);

    }

    private void loadViews() throws IOException {
        mainSplitPane.getItems().addAll(filesTableView(), songInfoPane());
        mainSplitPane.setDividerPosition(0, 0.6);
    }

    private Node filesTableView() throws IOException {
        return FILES_TABLE_VIEW.root();
    }

    private Node songInfoPane() throws IOException {
        return SONG_INFO_VIEW.root();
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