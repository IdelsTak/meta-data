package com.github.idelstak.metadata.views;

import com.github.idelstak.metadata.filesystem.*;
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

import static com.github.idelstak.metadata.views.Fxml.*;
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
        assertThat(controller.track()).isEqualTo(taggedFile.track());
        assertThat(controller.year()).isEqualTo(taggedFile.year());
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