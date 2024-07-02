package com.github.idelstak.metadata.views;

import javafx.stage.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.testfx.framework.junit5.*;

import java.io.*;
import java.util.concurrent.*;

import static javafx.application.Platform.*;
import static org.assertj.core.api.Assertions.*;
import static org.testfx.util.WaitForAsyncUtils.*;

@ExtendWith(ApplicationExtension.class)
class MetadataFetchViewTest {

    private MetadataFetchViewController controller;

    @Start
    void setUp(Stage stage) {
    }

    @Test
    void lists() {
        QueryPair title = new QueryPair("TITLE", "");
        QueryPair artist = new QueryPair("ARTIST", "drake");
        MetadataQuery query = new MetadataQuery(title, artist, null, null);

        runLater(() -> {
            try {
                new MetadataFetchDialog(null, query, null).showAndWait();
            } catch (IOException e) {
                fail(e);
            }
        });

        waitForFxEvents();

        sleep(30, TimeUnit.SECONDS);
    }
}