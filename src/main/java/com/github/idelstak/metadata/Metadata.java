package com.github.idelstak.metadata;

import com.github.idelstak.metadata.components.*;
import javafx.application.*;
import javafx.scene.*;
import javafx.stage.*;
import org.slf4j.*;

public class Metadata extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(Metadata.class);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = Fxml.MAIN_VIEW.root();
        Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Metadata");
        primaryStage.show();

    }
}