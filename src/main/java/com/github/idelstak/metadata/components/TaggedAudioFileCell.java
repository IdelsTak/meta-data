package com.github.idelstak.metadata.components;

import com.dlsc.gemsfx.*;
import com.github.idelstak.metadata.model.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;

import static javafx.application.Platform.runLater;

public class TaggedAudioFileCell extends TableCell<List<TaggedAudioFile>, TaggedAudioFile> {

    private final HBox root = new HBox();
    private final AvatarView view = new AvatarView();

    {
        view.setSize(160.0f);
        root.getStyleClass().add("result-box");
        root.setAlignment(Pos.CENTER);
        root.getChildren().add(view);

        view.setOnMouseClicked(_ -> {
            TableView.TableViewSelectionModel<List<TaggedAudioFile>> model = getTableView().getSelectionModel();
            model.clearAndSelect(0, getTableColumn());
        });
    }

    @Override
    protected void updateItem(TaggedAudioFile taggedAudioFile, boolean empty) {
        super.updateItem(taggedAudioFile, empty);

        if (empty || taggedAudioFile == null) {
            setGraphic(null);
        } else {
            runLater(() -> view.setImage(taggedAudioFile.art()));
            setGraphic(root);
        }
    }
}