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
import java.util.*;
import java.util.concurrent.*;

import static com.github.idelstak.metadata.filesystem.SampleFile.*;
import static com.github.idelstak.metadata.components.Fxml.*;
import static org.assertj.core.api.Assertions.*;
import static org.testfx.framework.junit5.utils.FXUtils.*;
import static org.testfx.util.WaitForAsyncUtils.*;

@ExtendWith(ApplicationExtension.class)
class FilesTableViewTest {

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
        File sampleFilesDirectory = new File(MIXED.url().getFile());
        controller.setDirectory(sampleFilesDirectory);

        waitForFxEvents();

        List<File> audioFiles = new FileAccess(sampleFilesDirectory).getAudioFiles();
        assertThat(audioFiles).isNotNull();

        assertThat(controller.audioFilesCount()).isEqualTo(audioFiles.size());
    }

    @Test
    void displays_metadata() throws InterruptedException {
        assertThat(MP3S.url()).isNotNull();

        File directory = new File(MP3S.url().getFile());
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

        Assertions.assertThat(view).containsRow("The Weeknd", "Too Late", "After Hours");

        view.getItems().removeListener(itemsLoadedListener);
    }

}