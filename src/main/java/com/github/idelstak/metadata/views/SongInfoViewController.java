package com.github.idelstak.metadata.views;

import com.dlsc.gemsfx.*;
import com.github.idelstak.metadata.components.*;
import com.github.idelstak.metadata.model.*;
import javafx.beans.property.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import org.slf4j.*;

import java.io.*;

import static javafx.application.Platform.*;

public class SongInfoViewController extends FxmlController {

    private static final Logger LOG = LoggerFactory.getLogger(SongInfoViewController.class);
    private final ObjectProperty<TaggedAudioFile> taggedAudioFile;
    @FXML
    private TextField albumField;
    @FXML
    private TextField titleField;
    @FXML
    private TextField artistField;
    @FXML
    private AvatarView artView;
    @FXML
    private TextField fileNameField;

    public SongInfoViewController() {taggedAudioFile = new SimpleObjectProperty<>();}

    @Override
    protected void initialize() {
        taggedAudioFile.addListener((_, _, taggedAudioFile) -> {
            runLater(() -> {
                if (taggedAudioFile == null) {
                    titleField.setText(null);
                    artistField.setText(null);
                    albumField.setText(null);
                    fileNameField.setText(null);
                    artView.setImage(null);
                } else {
                    titleField.setText(taggedAudioFile.title());
                    artistField.setText(taggedAudioFile.artist());
                    albumField.setText(taggedAudioFile.album());
                    fileNameField.setText(taggedAudioFile.fileName());
                    artView.setImage(taggedAudioFile.art());
                }
            });
        });
    }

    void setTaggedAudioFile(TaggedAudioFile taggedAudioFile) {
        this.taggedAudioFile.set(taggedAudioFile);
    }

    TaggedAudioFile taggedAudioFile() {
        return taggedAudioFile.get();
    }

    TaggedAudioFile updateTaggedAudioFile() throws IOException {
        TaggedAudioFile editedFile = new TaggedAudioFile(title(), artist(), album(), art(), fileName());
        TaggedAudioFile updatedFile = this.taggedAudioFile.get();
        updatedFile.writeFrom(editedFile);
        return updatedFile;
    }

    String title() {
        return titleField.getText();
    }

    String artist() {
        return artistField.getText();
    }

    String album() {
        return albumField.getText();
    }

    Image art() {
        return artView.getImage();
    }

    String fileName() {
        return fileNameField.getText();
    }
}