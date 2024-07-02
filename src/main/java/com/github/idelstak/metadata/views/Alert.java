package com.github.idelstak.metadata.views;

import javafx.scene.control.*;
import javafx.stage.*;

public enum Alert {
    ERROR(javafx.scene.control.Alert.AlertType.ERROR);

    private final javafx.scene.control.Alert alert;
    private final DialogPane dialogPane;

    Alert(javafx.scene.control.Alert.AlertType type) {
        alert = new javafx.scene.control.Alert(type);
        dialogPane = alert.getDialogPane();
    }

    void show(Window owner, String message, Exception exception) {
        show(owner, message, null, exception);
    }

    void show(Window owner, String header, String content, Exception exception) {
        alert.initOwner(owner);
        alert.setHeaderText(header);
        if (exception != null) {
            Label label = new Label(exception.getMessage());
            label.getStyleClass().addAll("error", "expandable");
            dialogPane.setExpandableContent(label);
            dialogPane.setExpanded(true);
        } else {
            alert.setContentText(content);
        }

        alert.show();
    }
}