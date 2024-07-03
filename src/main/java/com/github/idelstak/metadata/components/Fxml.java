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
package com.github.idelstak.metadata.components;

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

    public Object controller() throws IOException {
        if (root == null) {
            load();
        }
        return controller;
    }

}