package com.github.idelstak.metadata.views;

import com.github.idelstak.metadata.filesystem.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.testfx.assertions.api.Assertions;
import org.testfx.framework.junit5.*;

import java.io.*;
import java.util.*;

import static com.github.idelstak.metadata.views.Fxml.*;
import static org.assertj.core.api.Assertions.*;
import static org.testfx.framework.junit5.utils.FXUtils.*;
import static org.testfx.util.WaitForAsyncUtils.*;

@ExtendWith(ApplicationExtension.class)
class FilesTableViewTest extends FileAccessTest {

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
    void loadsAudioFilesOnly() throws IOException {
        File sampleFilesDirectory = new File(sampleFilesUrl().getFile());
        controller.setDirectory(sampleFilesDirectory);

        waitForFxEvents();

        List<File> audioFiles = new FileAccess(sampleFilesDirectory).getAudioFiles();
        assertThat(audioFiles).isNotNull();

        TableView<Object> view = controller.getFilesView();

        Assertions.assertThat(view).hasExactlyNumRows(audioFiles.size());
    }

}