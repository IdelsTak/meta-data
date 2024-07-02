package com.github.idelstak.metadata.views;

import com.dlsc.gemsfx.*;
import javafx.beans.property.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import org.slf4j.*;

import static javafx.application.Platform.*;

public class SongInfoViewController extends FxmlController {

    private static final Logger LOG = LoggerFactory.getLogger(SongInfoViewController.class);
    private final ObjectProperty<TaggedAudioFile> taggedAudioFile;
    @FXML
    private TextField albumField;
    @FXML
    private TextField trackField;
    @FXML
    private TextField yearField;
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
                    trackField.setText(null);
                    yearField.setText(null);
                    fileNameField.setText(null);
                    artView.setImage(null);
                } else {
                    titleField.setText(taggedAudioFile.title());
                    artistField.setText(taggedAudioFile.artist());
                    albumField.setText(taggedAudioFile.album());
                    trackField.setText(taggedAudioFile.track());
                    yearField.setText(taggedAudioFile.year());
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

    TaggedAudioFile updatedTaggedAudioFile() {
        TaggedAudioFile editedFile = new TaggedAudioFile(title(),
                                                         artist(),
                                                         album(),
                                                         track(),
                                                         year(),
                                                         art(),
                                                         fileName());
        TaggedAudioFile updatedFile = this.taggedAudioFile.get();
        updatedFile.copy(editedFile);
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

    String track() {
        return trackField.getText();
    }

    String year() {
        return yearField.getText();
    }

    Image art() {
        return artView.getImage();
    }

    String fileName() {
        return fileNameField.getText();
    }
}