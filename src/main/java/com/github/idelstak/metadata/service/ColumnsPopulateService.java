package com.github.idelstak.metadata.service;

import com.github.idelstak.metadata.components.*;
import com.github.idelstak.metadata.model.*;
import javafx.beans.property.*;
import javafx.concurrent.*;
import javafx.scene.control.*;

import java.util.*;

import static javafx.application.Platform.runLater;

public class ColumnsPopulateService extends Service<List<TableColumn<List<TaggedAudioFile>, TaggedAudioFile>>> {

    private final List<TaggedAudioFile> taggedAudioFiles;

    public ColumnsPopulateService(List<TaggedAudioFile> taggedAudioFiles) {
        this.taggedAudioFiles = taggedAudioFiles;
    }

    @Override
    protected Task<List<TableColumn<List<TaggedAudioFile>, TaggedAudioFile>>> createTask() {
        return new Task<>() {
            @Override
            protected List<TableColumn<List<TaggedAudioFile>, TaggedAudioFile>> call() {
                var columns = new ArrayList<TableColumn<List<TaggedAudioFile>, TaggedAudioFile>>();

                for (int j = 0; j < taggedAudioFiles.size(); j++) {
                    int i = j;
                    var column = new TableColumn<List<TaggedAudioFile>, TaggedAudioFile>();
                    column.setCellValueFactory(f -> {
                        return new SimpleObjectProperty<>(f.getValue().get(i));
                    });
                    column.setCellFactory(_ -> new TaggedAudioFileCell());
                    runLater(() -> updateValue(List.of(column)));
                    columns.add(column);
                }

                return columns;
            }
        };
    }
}