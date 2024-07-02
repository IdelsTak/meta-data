package com.github.idelstak.metadata.views;

import javafx.fxml.*;
import javafx.scene.*;

import java.io.*;
import java.net.*;

public enum Fxml {
    FILES_TABLE_VIEW("/com/github/idelstak/metadata/views/files-table-view.fxml"),
    SONG_INFO_VIEW("/com/github/idelstak/metadata/views/song-info-view.fxml"),
    MAIN_VIEW("/com/github/idelstak/metadata/views/main-view.fxml"),
    METADATA_FETCH_VIEW("/com/github/idelstak/metadata/views/metadata-fetch-view.fxml");

    private final String path;
    private Object controller;
    private Parent root;

    Fxml(String path) {this.path = path;}

    public Parent root() throws IOException {
        load();
        return root;
    }

    private void load() throws IOException {
        URL location = getClass().getResource(path);
        FXMLLoader loader = new FXMLLoader(location);
        root = loader.load();
        controller = loader.getController();
    }

    Object controller() throws IOException {
        if (root == null) {
            load();
        }
        return controller;
    }

}