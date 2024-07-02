package com.github.idelstak.metadata.views;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.*;

import java.io.*;

import static com.github.idelstak.metadata.views.Css.*;
import static com.github.idelstak.metadata.views.Fxml.*;

public class MetadataFetchDialog extends Dialog<Void> {

    private final MetadataQuery query;
    private final TaggedAudioFile taggedAudioFile;

    public MetadataFetchDialog(Window owner, MetadataQuery query, TaggedAudioFile taggedAudioFile) throws IOException {
        this.query = query;
        this.taggedAudioFile = taggedAudioFile;

        setTitle("Fetch Metadata");
        initOwner(owner);

        setDialogPane(dialogPane());
    }

    private DialogPane dialogPane() throws IOException {
        Node root = METADATA_FETCH_VIEW.root();
        MetadataFetchViewController controller = (MetadataFetchViewController) METADATA_FETCH_VIEW.controller();
        controller.setQuery(query);
        controller.setTaggedAudioFile(taggedAudioFile);

        DialogPane pane = new DialogPane();
        pane.getStylesheets().addLast(STYLES.toExternalForm());
        pane.setPrefWidth(900.0f);
        pane.setPrefHeight(700.0f);
        pane.setMinWidth(900.0f);
        pane.setMinHeight(700.0f);
        pane.setMaxWidth(900.0f);
        pane.setMaxHeight(700.0f);
        pane.setContent(root);
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        return pane;
    }
}