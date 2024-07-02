module com.github.idelstak.metadata {
    requires org.slf4j;
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.controls;
    requires jaudiotagger;
    requires com.dlsc.gemsfx;
    requires java.desktop;
    requires org.kordamp.ikonli.boxicons;
    requires org.kordamp.ikonli.microns;
    requires org.json;
    requires okhttp3;
    requires javafx.swing;

    exports com.github.idelstak.metadata to javafx.graphics;
    exports com.github.idelstak.metadata.views to javafx.fxml;
    exports com.github.idelstak.metadata.filesystem;

    opens com.github.idelstak.metadata to javafx.graphics;
    opens com.github.idelstak.metadata.views to javafx.fxml;
    opens com.github.idelstak.metadata.filesystem;
}