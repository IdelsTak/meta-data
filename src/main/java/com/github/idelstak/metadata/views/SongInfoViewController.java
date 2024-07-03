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
//        renameFileButton.disableProperty()
//                        .bind(useTitleCheck.selectedProperty()
//                                           .not()
//                                           .and(useArtistCheck.selectedProperty().not())
//                                           .and(useAlbumCheck.selectedProperty().not()));
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

//    @Deprecated
//    private void renameFileFromTags(ActionEvent actionEvent) {
//        StringJoiner joiner = new StringJoiner(" - ");
//        String title = useTitleCheck.isSelected() ? title() : "";
//        String artist = useArtistCheck.isSelected() ? artist() : "";
//        String album = useAlbumCheck.isSelected() ? album() : "";
//        if (title != null && !title.isBlank()) {
//            joiner.add(title);
//        }
//        if (artist != null && !artist.isBlank()) {
//            joiner.add(artist);
//        }
//        if (album != null && !album.isBlank()) {
//            joiner.add(album);
//        }
//        String generatedName = joiner.toString();
//        String mime = fileExtension(fileNameField.getText());
//        fileNameField.setText(generatedName + '.' + mime);
//        actionEvent.consume();
//    }
//
//    private static String fileExtension(String fileName) {
//        int lastDotIndex = fileName.lastIndexOf('.');
//        if (lastDotIndex != -1 && lastDotIndex < fileName.length() - 1) {
//            return fileName.substring(lastDotIndex + 1);
//        }
//        return "";
//    }

}