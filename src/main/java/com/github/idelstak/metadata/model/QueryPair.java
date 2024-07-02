package com.github.idelstak.metadata.model;

import javafx.util.*;

public class QueryPair extends Pair<String, String> {

    public QueryPair(String key, String value) {super(key, value);}

    @Override
    public String toString() {
        return "%s: %s".formatted(getKey(), getValue());
    }
}