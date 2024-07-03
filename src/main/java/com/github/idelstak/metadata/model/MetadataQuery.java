package com.github.idelstak.metadata.model;

import javafx.util.*;

public record MetadataQuery(
        Pair<String, String> title, Pair<String, String> artist, Pair<String, String> album
) {}