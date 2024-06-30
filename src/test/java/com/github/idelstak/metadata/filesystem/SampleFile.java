package com.github.idelstak.metadata.filesystem;

import java.net.*;

public enum SampleFile {
    MP3S("/mp3s"),
    MIXED("/mixed");

    private final URL url;

    SampleFile(String name) {this.url = SampleFile.class.getResource(name);}

    public URL url() {
        return url;
    }
}