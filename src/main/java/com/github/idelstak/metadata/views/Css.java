package com.github.idelstak.metadata.views;

import java.net.*;

public enum Css {
    STYLES("/com/github/idelstak/metadata/views/styles.css");

    private final String path;

    Css(String path) {this.path = path;}

    String toExternalForm() {
        URL resource = getClass().getResource(path);
        assert resource != null;
        return resource.toExternalForm();
    }
}