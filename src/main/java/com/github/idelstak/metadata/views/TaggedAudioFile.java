package com.github.idelstak.metadata.views;

import javafx.beans.property.*;
import javafx.scene.image.*;
import org.jaudiotagger.audio.*;
import org.jaudiotagger.tag.*;
import org.jaudiotagger.tag.datatype.*;
import org.slf4j.*;

import javax.imageio.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import static org.jaudiotagger.tag.FieldKey.*;

public class TaggedAudioFile {

    private static final Logger LOG = LoggerFactory.getLogger(TaggedAudioFile.class);
    private final StringProperty title;
    private final StringProperty album;
    private final StringProperty artist;
    private final StringProperty year;
    private final StringProperty track;
    private final StringProperty fileName;
    private final ObjectProperty<Image> art;

    public TaggedAudioFile(AudioFile audioFile) {
        title = new SimpleStringProperty();
        artist = new SimpleStringProperty();
        album = new SimpleStringProperty();
        track = new SimpleStringProperty();
        year = new SimpleStringProperty();
        fileName = new SimpleStringProperty(audioFile.getFile().getName());
        art = new SimpleObjectProperty<>();

        Tag tag = audioFile.getTag();
        if (tag != null) {
            title.set(tag.getFirst(TITLE));
            artist.set(tag.getFirst(ARTIST));
            album.set(tag.getFirst(ALBUM));
            track.set(tag.getFirst(TRACK));
            year.set(tag.getFirst(YEAR));
            try {
                art.set(firstArtWork(tag));
            } catch (IOException e) {
                LOG.error("", e);
            }
        }
    }

    private static Image firstArtWork(Tag tag) throws IOException {
        Artwork artwork = tag.getFirstArtwork();
        if (artwork == null) {
            return null;
        }
        return image(artwork.getImage());
    }

    private static Image image(BufferedImage image) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", out);
            byte[] buffer = out.toByteArray();
            try (ByteArrayInputStream in = new ByteArrayInputStream(buffer)) {
                return new Image(in);
            }
        }
    }

    @Override
    public String toString() {
        String prefix = TaggedAudioFile.class.getSimpleName() + "[";
        StringJoiner joiner = new StringJoiner(", ", prefix, "]");
        return joiner.add("title=" + title.get())
                     .add("album=" + album.get())
                     .add("artist=" + artist.get())
                     .add("year=" + year.get())
                     .add("track=" + track.get())
                     .add("fileName=" + fileName.get())
                     .add("art=" + art.get())
                     .toString();
    }

    StringProperty titleProperty() {
        return title;
    }

    StringProperty artistProperty() {
        return artist;
    }

    String track() {
        return track.get();
    }

    String artist() {
        return artist.get();
    }

    String title() {
        return title.get();
    }

    String album() {
        return album.get();
    }

    String year() {
        return year.get();
    }

    String fileName() {
        return fileName.get();
    }

    StringProperty albumProperty() {
        return album;
    }

    StringProperty trackProperty() {
        return track;
    }

    StringProperty yearProperty() {
        return year;
    }

    StringProperty fileNameProperty() {
        return fileName;
    }

    ObjectProperty<Image> artProperty() {
        return art;
    }

    Image art() {
        return art.get();
    }
}