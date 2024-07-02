package com.github.idelstak.metadata.views;

import javafx.fxml.*;

import java.io.*;

public abstract class FxmlController {

    @FXML
    protected abstract void initialize() throws IOException;
}