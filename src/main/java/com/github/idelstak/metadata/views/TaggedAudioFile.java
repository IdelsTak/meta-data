package com.github.idelstak.metadata.views;

import org.jaudiotagger.audio.*;
import org.jaudiotagger.tag.*;

import java.util.*;

import static org.jaudiotagger.tag.FieldKey.*;

public class TaggedAudioFile {

    private String title;
    private String album;
    private String year;
    private String artist;
    private String track;

    public TaggedAudioFile(AudioFile audioFile) {
        Tag tag = audioFile.getTag();
        if (tag != null) {
            track = tag.getFirst(TRACK);
            artist = tag.getFirst(ARTIST);
            title = tag.getFirst(TITLE);
            album = tag.getFirst(ALBUM);
            year = tag.getFirst(YEAR);
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TaggedAudioFile.class.getSimpleName() + "[", "]").add("album='" + album + "'")
                                                                                       .add("title='" + title + "'")
                                                                                       .add("year='" + year + "'")
                                                                                       .add("artist='" + artist + "'")
                                                                                       .add("track='" + track + "'")
                                                                                       .toString();
    }

    String album() {
        return album;
    }

    String artist() {
        return artist;
    }

    String title() {
        return title;
    }

    String track() {
        return track;
    }

    String year() {
        return year;
    }
}