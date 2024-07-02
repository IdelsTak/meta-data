package com.github.idelstak.metadata.model;

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
    private final StringProperty fileName;
    private final ObjectProperty<Image> art;
    private AudioFile audioFile;

    public TaggedAudioFile(AudioFile audioFile) {
        this.audioFile = audioFile;
        title = new SimpleStringProperty();
        artist = new SimpleStringProperty();
        album = new SimpleStringProperty();
        fileName = new SimpleStringProperty(audioFile.getFile().getName());
        art = new SimpleObjectProperty<>();

        Tag tag = audioFile.getTag();
        if (tag != null) {
            title.set(tag.getFirst(TITLE));
            artist.set(tag.getFirst(ARTIST));
            album.set(tag.getFirst(ALBUM));
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

    public TaggedAudioFile(String title, String artist, String album, Image art, String file) {
        this.title = new SimpleStringProperty(title);
        this.artist = new SimpleStringProperty(artist);
        this.album = new SimpleStringProperty(album);
        this.art = new SimpleObjectProperty<>(art);
        this.fileName = new SimpleStringProperty(file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, album, artist, fileName, art);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaggedAudioFile that)) return false;
        return Objects.equals(title, that.title) &&
               Objects.equals(album, that.album) &&
               Objects.equals(artist, that.artist) &&
               Objects.equals(fileName, that.fileName) &&
               Objects.equals(art, that.art);
    }

    @Override
    public String toString() {
        String prefix = TaggedAudioFile.class.getSimpleName() + "[";
        StringJoiner joiner = new StringJoiner(", ", prefix, "]");
        return joiner.add("title=" + title.get())
                     .add("artist=" + artist.get())
                     .add("album=" + album.get())
                     .add("fileName=" + fileName.get())
                     .add("art=" + art.get())
                     .toString();
    }

    public void writeFrom(TaggedAudioFile taggedAudioFile) throws IOException {
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
            setArtwork(tag, taggedAudioFile.art());
            AudioFileIO.write(audioFile);
        } catch (FieldDataInvalidException | CannotWriteException e) {
            throw new IOException(e);
        }

        taggedAudioFile.fileName.set(fileName());

        copy(taggedAudioFile);
    }

    public String title() {
        return title.get();
    }

    public String artist() {
        return artist.get();
    }

    public String album() {
        return album.get();
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

    public Image art() {
        return art.get();
    }

    public String fileName() {
        return fileName.get();
    }

    private void copy(TaggedAudioFile taggedAudioFile) {
        title.set(taggedAudioFile.title());
        artist.set(taggedAudioFile.artist());
        album.set(taggedAudioFile.album());
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