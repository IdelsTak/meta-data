package com.github.idelstak.metadata.views;

import com.github.idelstak.metadata.filesystem.*;
import javafx.collections.*;
import javafx.collections.ListChangeListener.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.testfx.assertions.api.Assertions;
import org.testfx.framework.junit5.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import static com.github.idelstak.metadata.views.Fxml.*;
import static org.assertj.core.api.Assertions.*;
import static org.testfx.framework.junit5.utils.FXUtils.*;
import static org.testfx.util.WaitForAsyncUtils.*;

@ExtendWith(ApplicationExtension.class)
class FilesTableViewTest extends FilesAccessTest {

    private FilesTableViewController controller;

    @Start
    void init(Stage stage) {
        runFX(() -> {

            Parent root = null;
            try {
                root = FILES_TABLE_VIEW.root();
                controller = (FilesTableViewController) FILES_TABLE_VIEW.controller();
            } catch (IOException e) {
                fail(e);
            }

            stage.setScene(new Scene(root));
            stage.show();
            stage.toFront();
        });

        waitForFxEvents();
    }

    @Test
    void loads_audio_files_only() throws IOException {
        File sampleFilesDirectory = new File(sampleFilesUrl().getFile());
        controller.setDirectory(sampleFilesDirectory);

        waitForFxEvents();

        List<File> audioFiles = new FileAccess(sampleFilesDirectory).getAudioFiles();
        assertThat(audioFiles).isNotNull();

        assertThat(controller.audioFilesCount()).isEqualTo(audioFiles.size());
    }

    @Test
    void displaysMetadata() throws InterruptedException {
        URL url = mp3sDir();
        assertThat(url).isNotNull();

        File directory = new File(url.getFile());
        controller.setDirectory(directory);

        waitForFxEvents();

        CountDownLatch itemsLoadedLatch = new CountDownLatch(1);
        TableView<Object> view = controller.getFilesView();

        ListChangeListener<Object> itemsLoadedListener = (Change<?> change) -> {
            if (change.next() && change.wasAdded() && change.getList().size() == controller.audioFilesCount()) {
                itemsLoadedLatch.countDown();
            }
        };
        view.getItems().addListener(itemsLoadedListener);

        // Make the current thread to wait until the latch has counted down to 0;
        // i.e., the TableView items have been fully loaded
        itemsLoadedLatch.await();

        Assertions.assertThat(view).containsRow("2", "The Weeknd", "Too Late", "After Hours", "2020");

        view.getItems().removeListener(itemsLoadedListener);
    }

}