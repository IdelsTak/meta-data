/*
 * MIT License
 *
 * Copyright (c) 2024 Hiram K
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sub-license, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
module com.github.idelstak.metadata {
    requires org.slf4j;
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.swing;
    requires jaudiotagger;
    requires com.dlsc.gemsfx;
    requires java.desktop;
    requires org.kordamp.ikonli.boxicons;
    requires org.kordamp.ikonli.microns;
    requires org.kordamp.ikonli.codicons;
    requires org.json;
    requires okhttp3;

    exports com.github.idelstak.metadata to javafx.graphics;
    exports com.github.idelstak.metadata.views to javafx.fxml;
    exports com.github.idelstak.metadata.filesystem;

    opens com.github.idelstak.metadata to javafx.graphics;
    opens com.github.idelstak.metadata.views to javafx.fxml;
    opens com.github.idelstak.metadata.filesystem;
    exports com.github.idelstak.metadata.components to javafx.fxml;
    opens com.github.idelstak.metadata.components to javafx.fxml;
    exports com.github.idelstak.metadata.model to javafx.fxml;
    opens com.github.idelstak.metadata.model to javafx.fxml;
}