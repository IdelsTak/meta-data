package com.github.idelstak.metadata.views;

import javafx.beans.property.*;
import javafx.embed.swing.*;
import javafx.scene.image.*;
import org.jaudiotagger.audio.*;
import org.jaudiotagger.audio.exceptions.*;
import org.jaudiotagger.tag.*;
import org.jaudiotagger.tag.datatype.*;
import org.jaudiotagger.tag.id3.valuepair.*;
import org.jaudiotagger.tag.reference.*;
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
    private AudioFile audioFile;

    public TaggedAudioFile(AudioFile audioFile) {
        this.audioFile = audioFile;
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

    public TaggedAudioFile(
            String title, String artist, String album, String track, String year, Image art, String fileName) {
        this.title = new SimpleStringProperty(title);
        this.artist = new SimpleStringProperty(artist);
        this.album = new SimpleStringProperty(album);
        this.track = new SimpleStringProperty(track);
        this.year = new SimpleStringProperty(year);
        this.art = new SimpleObjectProperty<>(art);
        this.fileName = new SimpleStringProperty(fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, album, artist, year, track, fileName, art);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaggedAudioFile that)) return false;
        return Objects.equals(title, that.title) &&
               Objects.equals(album, that.album) &&
               Objects.equals(artist, that.artist) &&
               Objects.equals(year, that.year) &&
               Objects.equals(track, that.track) &&
               Objects.equals(fileName, that.fileName) &&
               Objects.equals(art, that.art);
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

    void writeFrom(TaggedAudioFile taggedAudioFile) throws IOException {
        if (audioFile == null) {
            throw new IOException(new IllegalArgumentException("Attempt to use null TaggedAudioFile"));
        }

        Tag tag = audioFile.getTag();
        if (tag == null) {
            throw new IOException(new IllegalArgumentException("Attempt to use null Tag from AudioFile"));
        }

        try {
            tag.setField(TITLE, taggedAudioFile.title());
            tag.setField(ARTIST, taggedAudioFile.artist());
            tag.setField(ALBUM, taggedAudioFile.album());
            String track = taggedAudioFile.track();
            if (track != null) {
                try {
                    int parsedInt = Integer.parseInt(track);
                    tag.setField(TRACK, Integer.toString(parsedInt));
                } catch (NumberFormatException e) {
                    LOG.warn("", e);
                }
            }
            String year = taggedAudioFile.year();
            if (year != null) {
                try {
                    int parsedInt = Integer.parseInt(year);
                    tag.setField(YEAR, Integer.toString(parsedInt));
                } catch (NumberFormatException e) {
                    LOG.warn("", e);
                }
            }
            setArtwork(tag, taggedAudioFile.art());
            AudioFileIO.write(audioFile);
        } catch (FieldDataInvalidException | CannotWriteException e) {
            throw new IOException(e);
        }

        taggedAudioFile.fileName.set(fileName());

        copy(taggedAudioFile);
    }

    String title() {
        return title.get();
    }

    String artist() {
        return artist.get();
    }

    String album() {
        return album.get();
    }

    String track() {
        return track.get();
    }

    String year() {
        return year.get();
    }

    private static void setArtwork(Tag tag, Image art) throws IOException, FieldDataInvalidException {
        Artwork artwork = new Artwork();
        byte[] imageData = imageData(art);
        artwork.setBinaryData(imageData);
        artwork.setMimeType(ImageFormats.getMimeTypeForBinarySignature(imageData));
        artwork.setDescription("");
        artwork.setPictureType(PictureTypes.DEFAULT_ID);

        tag.deleteArtworkField();
        tag.addField(artwork);
    }

    Image art() {
        return art.get();
    }

    String fileName() {
        return fileName.get();
    }

    void copy(TaggedAudioFile taggedAudioFile) {
        title.set(taggedAudioFile.title());
        artist.set(taggedAudioFile.artist());
        album.set(taggedAudioFile.album());
        track.set(taggedAudioFile.track());
        year.set(taggedAudioFile.year());
        art.set(taggedAudioFile.art());
        fileName.set(taggedAudioFile.fileName());
    }

    private static byte[] imageData(Image art) throws IOException {
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(art, null);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(bufferedImage, "png", out);
            return out.toByteArray();
        }
    }

}