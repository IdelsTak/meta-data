package com.github.idelstak.metadata.views;

import javafx.util.*;

public record MetadataQuery(
        Pair<String, String> title, Pair<String, String> artist, Pair<String, String> album, Pair<String, String> year
) {}