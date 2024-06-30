module com.github.idelstak.metadata {
    requires org.slf4j;
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.controls;
    requires jaudiotagger;

    exports com.github.idelstak.metadata.views to javafx.fxml;
    exports com.github.idelstak.metadata.filesystem;

    opens com.github.idelstak.metadata.views to javafx.fxml;
    opens com.github.idelstak.metadata.filesystem;
}