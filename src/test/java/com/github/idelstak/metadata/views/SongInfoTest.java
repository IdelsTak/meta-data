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
import com.github.idelstak.metadata.model.*;
import com.github.idelstak.metadata.service.*;
import javafx.scene.*;
import javafx.stage.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.slf4j.*;
import org.testfx.framework.junit5.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import static com.github.idelstak.metadata.components.Fxml.*;
import static javafx.application.Platform.*;
import static org.assertj.core.api.Assertions.*;
import static org.testfx.util.WaitForAsyncUtils.*;

@ExtendWith(ApplicationExtension.class)
class SongInfoTest {

    private static final Logger LOG = LoggerFactory.getLogger(SongInfoTest.class);
    private SongInfoViewController controller;

    @Start
    void setUp(Stage stage) {
        runLater(() -> {
            Parent root = null;
            try {
                root = SONG_INFO_VIEW.root();
                controller = (SongInfoViewController) SONG_INFO_VIEW.controller();
            } catch (IOException e) {
                fail(e);
            }
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
            stage.toFront();
        });

        waitForFxEvents();
    }

    @Test
    void displays_song_info() throws IOException, InterruptedException {
        File audioFile = sampleMp3();
        CountDownLatch latch = new CountDownLatch(1);
        AudioFileResolveService service = new AudioFileResolveService(List.of(audioFile));

        service.setOnSucceeded(_ -> latch.countDown());
        service.start();
        latch.await();

        List<TaggedAudioFile> audioFiles = waitForAsyncFx(10L, service::getValue);
        TaggedAudioFile taggedFile = audioFiles.getFirst();

        controller.setTaggedAudioFile(taggedFile);

        waitForFxEvents();

        assertThat(controller.title()).isEqualTo(taggedFile.title());
        assertThat(controller.artist()).isEqualTo(taggedFile.artist());
        assertThat(controller.album()).isEqualTo(taggedFile.album());
        assertThat(controller.fileName()).isEqualTo(taggedFile.fileName());
        assertThat(controller.art()).isEqualTo(taggedFile.art());
    }

    private static File sampleMp3() throws IOException {
        URL resource = SongInfoTest.class.getResource("/mp3s");
        assertThat(resource).isNotNull();

        FileAccess fileAccess = new FileAccess(new File(resource.getFile()));
        List<File> files = fileAccess.getAudioFiles();
        assertThat(files).isNotEmpty();

        return files.getFirst();
    }
}