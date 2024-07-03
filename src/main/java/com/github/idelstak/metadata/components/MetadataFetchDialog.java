package com.github.idelstak.metadata.components;

import com.github.idelstak.metadata.model.*;
import com.github.idelstak.metadata.views.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.*;
import org.slf4j.*;

import java.io.*;

import static com.github.idelstak.metadata.components.Css.*;
import static com.github.idelstak.metadata.components.Fxml.*;
import static javafx.application.Platform.*;

public class MetadataFetchDialog extends Dialog<ButtonType> {

    private static final Logger LOG = LoggerFactory.getLogger(MetadataFetchViewController.class);
    private MetadataFetchViewController controller;

    public MetadataFetchDialog(Window owner, MetadataQuery query, TaggedAudioFile taggedAudioFile) throws IOException {
        setTitle("Fetch Metadata");
        initOwner(owner);
        setDialogPane(dialogPane(query, taggedAudioFile));

        setOnHiding(_ -> {
            if (controller != null) {
                controller.cancelFetchProperty().setValue(null);
                controller.cancelFetchProperty().set(true);
            }
        });
        setResultConverter(param -> {
            return param.getButtonData() == ButtonBar.ButtonData.OK_DONE ? ButtonType.OK : ButtonType.CANCEL;
        });
    }

    private DialogPane dialogPane(MetadataQuery query, TaggedAudioFile taggedAudioFile) throws IOException {
        Node root = METADATA_FETCH_VIEW.root();
        controller = (MetadataFetchViewController) METADATA_FETCH_VIEW.controller();
        controller.setQuery(query);
        controller.setTaggedAudioFile(taggedAudioFile);

        DialogPane pane = new DialogPane();
        pane.getStylesheets().addLast(STYLES.toExternalForm());
        pane.setPrefWidth(900.0f);
        pane.setPrefHeight(800.0f);
        pane.setMinWidth(900.0f);
        pane.setMinHeight(800.0f);
        pane.setMaxWidth(900.0f);
        pane.setMaxHeight(800.0f);
        pane.setContent(root);
        ButtonType writeType = new ButtonType("Write", ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().addAll(writeType, ButtonType.CANCEL);

        runLater(() -> {
            Button writeButton = (Button) pane.lookupButton(writeType);
            if (writeButton == null) {
                return;
            }
            writeButton.setOnAction(event -> {
                try {
                    taggedAudioFile.writeFrom(controller.selectedTaggedAudioFile());
                } catch (IOException e) {
                    LOG.error("", e);
                    Window owner = writeButton.getScene().getWindow();
                    String message = "Failed to write tags to " + taggedAudioFile.fileName();
                    Alert.ERROR.show(owner, message, e);
                    event.consume();
                }
            });
        });

        return pane;
    }

}