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
package com.github.idelstak.metadata.views;

import com.dlsc.gemsfx.*;
import com.github.idelstak.metadata.components.*;
import com.github.idelstak.metadata.model.*;
import com.github.idelstak.metadata.service.*;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.beans.value.*;
import javafx.collections.ListChangeListener.*;
import javafx.collections.*;
import javafx.css.*;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.control.TableView.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.util.*;
import org.slf4j.*;

import java.util.*;
import java.util.stream.*;

import static java.util.stream.Collectors.*;
import static javafx.application.Platform.*;
import static javafx.collections.FXCollections.*;

public class MetadataFetchViewController extends FxmlController {

    private static final Logger LOG = LoggerFactory.getLogger(MetadataFetchViewController.class);
    private static final PseudoClass MATCHED = PseudoClass.getPseudoClass("matched");
    private final ObjectProperty<MetadataQuery> query;
    private final ObservableList<TaggedAudioFile> tagResults;
    private final ObjectProperty<TaggedAudioFile> taggedAudioFile;
    private final ObjectProperty<TaggedAudioFile> selectedTaggedAudioFile;
    private final BooleanProperty cancelFetch;
    private final BooleanProperty allIncludeChecksSelected;
    private final BooleanProperty noIncludeCheckSelected;
    private final BooleanProperty someIncludeChecksSelected;
    @FXML
    private Label finalResultsFetchedLabel;
    @FXML
    private Label fetchResultsLabel;
    @FXML
    private HBox fetchProgressBox;
    @FXML
    private Label fetchProgressLabel;
    @FXML
    private Label queryLabel;
    @FXML
    private TableView<List<TaggedAudioFile>> artResultsTable;
    @FXML
    private ProgressBar fetchProgressBar;
    @FXML
    private Label newTitleLabel;
    @FXML
    private Label originalArtistLabel;
    @FXML
    private Label originalAlbumLabel;
    @FXML
    private AvatarView newArtView;
    @FXML
    private Label originalTitleLabel;
    @FXML
    private Label newAlbumLabel;
    @FXML
    private Label newArtistLabel;
    @FXML
    private AvatarView originalArtView;
    @FXML
    private CheckBox includeArtistCheck;
    @FXML
    private CheckBox includeArtCheck;
    @FXML
    private CheckBox includeTitleCheck;
    @FXML
    private CheckBox includeAlbumCheck;
    @FXML
    private CheckBox includeAllCheck;
    @FXML
    private StackPane newArtViewPane;

    public MetadataFetchViewController() {
        query = new SimpleObjectProperty<>();
        tagResults = observableArrayList();
        taggedAudioFile = new SimpleObjectProperty<>();
        selectedTaggedAudioFile = new SimpleObjectProperty<>();
        cancelFetch = new SimpleBooleanProperty();
        allIncludeChecksSelected = new SimpleBooleanProperty();
        noIncludeCheckSelected = new SimpleBooleanProperty();
        someIncludeChecksSelected = new SimpleBooleanProperty();
    }

    public void setQuery(MetadataQuery query) {
        this.query.set(query);
    }

    public void setTaggedAudioFile(TaggedAudioFile taggedAudioFile) {
        this.taggedAudioFile.set(taggedAudioFile);
    }

    public TaggedAudioFile selectedTaggedAudioFile() {
        TaggedAudioFile selectedFile = selectedTaggedAudioFile.get();
        TaggedAudioFile setFile = taggedAudioFile.get();
        String title = includeTitleCheck.isSelected() ? selectedFile.title() : setFile.title();
        String artist = includeArtistCheck.isSelected() ? selectedFile.artist() : setFile.artist();
        String album = includeAlbumCheck.isSelected() ? selectedFile.album() : setFile.album();
        Image art = includeArtCheck.isSelected() ? selectedFile.art() : setFile.art();
        return new TaggedAudioFile(title, artist, album, art, setFile.fileName());
    }

    @Override
    protected void initialize() {
        finalResultsFetchedLabel.visibleProperty().bind(fetchProgressBox.visibleProperty().not());
        TableViewSelectionModel<List<TaggedAudioFile>> artResultsSelectionModel = artResultsTable.getSelectionModel();
        artResultsSelectionModel.cellSelectionEnabledProperty().set(true);

        originalTitleLabel.textProperty().bind(taggedAudioFile.map(TaggedAudioFile::title));
        originalArtistLabel.textProperty().bind(taggedAudioFile.map(TaggedAudioFile::artist));
        originalAlbumLabel.textProperty().bind(taggedAudioFile.map(TaggedAudioFile::album));
        originalArtView.imageProperty().bind(taggedAudioFile.map(TaggedAudioFile::art));

        newTitleLabel.textProperty().bind(selectedTaggedAudioFile.map(TaggedAudioFile::title));
        newArtistLabel.textProperty().bind(selectedTaggedAudioFile.map(TaggedAudioFile::artist));
        newAlbumLabel.textProperty().bind(selectedTaggedAudioFile.map(TaggedAudioFile::album));
        newArtView.imageProperty().bind(selectedTaggedAudioFile.map(TaggedAudioFile::art));

        selectedTaggedAudioFile.addListener((_, _, fetchedFile) -> {
            runLater(() -> {
                TaggedAudioFile originalFile = this.taggedAudioFile.get();

                String fetchedText = fetchedFile != null ? fetchedFile.title() : "";
                String originalText = originalFile != null ? originalFile.title() : "";
                boolean match = fetchedText.equalsIgnoreCase(originalText);
                newTitleLabel.pseudoClassStateChanged(MATCHED, match);
                includeTitleCheck.setSelected(!match);

                fetchedText = fetchedFile != null ? fetchedFile.artist() : "";
                originalText = originalFile != null ? originalFile.artist() : "";
                match = fetchedText.equalsIgnoreCase(originalText);
                newArtistLabel.pseudoClassStateChanged(MATCHED, match);
                includeArtistCheck.setSelected(!match);

                fetchedText = fetchedFile != null ? fetchedFile.album() : "";
                originalText = originalFile != null ? originalFile.album() : "";
                match = fetchedText.equalsIgnoreCase(originalText);
                newAlbumLabel.pseudoClassStateChanged(MATCHED, match);
                includeAlbumCheck.setSelected(!match);

                Image fetchedArt = fetchedFile != null ? fetchedFile.art() : null;
                Image originalArt = originalFile != null ? originalFile.art() : null;
                match = new ImageComparator(fetchedArt, originalArt).equals();
                newArtViewPane.pseudoClassStateChanged(MATCHED, match);
                includeArtCheck.setSelected(!match);

                artResultsTable.requestFocus();
            });
        });

        // Bind allSelected to true if all individual checkboxes are selected
        allIncludeChecksSelected.bind(Bindings.createBooleanBinding(() -> includeTitleCheck.isSelected() &&
                                                                          includeArtistCheck.isSelected() &&
                                                                          includeAlbumCheck.isSelected() &&
                                                                          includeArtCheck.isSelected(),
                                                                    includeTitleCheck.selectedProperty(),
                                                                    includeArtistCheck.selectedProperty(),
                                                                    includeAlbumCheck.selectedProperty(),
                                                                    includeArtCheck.selectedProperty()));
        // Bind noneSelected to true if none of the individual checkboxes are selected
        noIncludeCheckSelected.bind(Bindings.createBooleanBinding(() -> !(includeTitleCheck.isSelected() ||
                                                                          includeArtistCheck.isSelected() ||
                                                                          includeAlbumCheck.isSelected() ||
                                                                          includeArtCheck.isSelected()),
                                                                  includeTitleCheck.selectedProperty(),
                                                                  includeArtistCheck.selectedProperty(),
                                                                  includeAlbumCheck.selectedProperty(),
                                                                  includeArtCheck.selectedProperty()));
        // Update includeAllCheck selection based on allSelected property
        allIncludeChecksSelected.addListener((_, _, newValue) -> runLater(() -> {
            includeAllCheck.setSelected(newValue != null && newValue);
        }));
        // Bind someSelected to true if some (but not all) checkboxes are selected
        someIncludeChecksSelected.bind(noIncludeCheckSelected.or(allIncludeChecksSelected).not());
        // Update includeAllCheck indeterminate state based on someSelected property
        someIncludeChecksSelected.addListener((_, _, someSelected) -> runLater(() -> {
            includeAllCheck.setIndeterminate(someSelected != null && someSelected);
        }));
        // Update individual checkboxes based on includeAllCheck selection
        includeAllCheck.setOnAction(_ -> {
            if (includeAllCheck.isSelected()) {
                selectIncludeChecks(true);
            } else if (includeAllCheck.isIndeterminate()) {
                selectIncludeChecks(true);
                runLater(() -> {
                    includeAllCheck.setIndeterminate(false);
                    includeAllCheck.setSelected(true);
                });
            } else {
                selectIncludeChecks(false);
            }
        });

        //noinspection rawtypes
        artResultsSelectionModel.getSelectedCells().addListener((Change<? extends TablePosition> change) -> {
            if (change.next() && change.wasAdded()) {
                runLater(() -> {
                    @SuppressWarnings("rawtypes")
                    TablePosition position = change.getAddedSubList().getFirst();
                    @SuppressWarnings("rawtypes")
                    TableColumn column = position.getTableColumn();
                    int index = artResultsTable.getColumns().indexOf(column);
                    List<TaggedAudioFile> files = artResultsTable.getItems().getFirst();
                    TaggedAudioFile file = index >= 0 ? (files.isEmpty() ? null : files.get(index)) : null;

                    selectedTaggedAudioFile.set(file);
                });
            }
        });

        queryLabel.textProperty().bind(toQueryText());
        query.addListener((_, _, query) -> {
            if (query == null) {
                return;
            }

            FetchService service = new FetchService(query);
            cancelFetch.addListener((_, _, cancel) -> {
                if (cancel != null && cancel) {
                    service.cancel();
                }
            });
            finalResultsFetchedLabel.textProperty()
                                    .bind(service.workDoneProperty()
                                                 .map(Number::intValue)
                                                 .map("%d results found"::formatted));
            fetchProgressBox.visibleProperty().bind(service.runningProperty());
            fetchProgressBar.progressProperty().bind(service.progressProperty());
            fetchProgressLabel.textProperty().bind(service.messageProperty());
            fetchResultsLabel.textProperty()
                             .bind(service.workDoneProperty()
                                          .map(Number::intValue)
                                          .map(done -> "%d/%d fetched".formatted(done, (int) service.getTotalWork())));
            service.setOnFailed(event -> LOG.error("", event.getSource().getException()));
            service.valueProperty().addListener((_, _, files) -> {
                if (files != null && files.size() == 1) {
                    runLater(() -> tagResults.add(files.getFirst()));
                }
            });
            service.start();
        });

        tagResults.addListener((Change<? extends TaggedAudioFile> change) -> {
            if (change.next()) {
                List<TaggedAudioFile> files = change.getList().stream().map(TaggedAudioFile.class::cast).toList();
                //noinspection unchecked
                artResultsTable.getItems().setAll(files);
                runLater(() -> {
                    if (artResultsTable.getPlaceholder() == null) {
                        artResultsTable.setPlaceholder(new ProgressIndicator(-1));
                    }
                    artResultsTable.getColumns().clear();
                });

                var service = new ColumnsPopulateService(new ArrayList<>(change.getList()));
                service.valueProperty().addListener((_, _, columns) -> {
                    if (columns != null && columns.size() == 1) {
                        runLater(() -> artResultsTable.getColumns().add(columns.getFirst()));
                    }

                    new Thread(() -> {
                        try {
                            Thread.sleep(100L);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        runLater(() -> {
                            int size = artResultsTable.getColumns().size();
                            if (size > 0) {
                                artResultsSelectionModel.clearAndSelect(0, artResultsTable.getColumns().getFirst());
                                artResultsTable.scrollToColumnIndex(size - 1);
                            }
                        });
                    }).start();
                });
                service.start();
            }
        });

    }

    private void selectIncludeChecks(boolean select) {
        includeTitleCheck.setSelected(select);
        includeArtistCheck.setSelected(select);
        includeAlbumCheck.setSelected(select);
        includeArtCheck.setSelected(select);
    }

    private ObservableValue<String> toQueryText() {
        return query.map(metadataQuery -> Stream.of(metadataQuery.title(),
                                                    metadataQuery.artist(),
                                                    metadataQuery.album()))
                    .map(stream -> stream.filter(Objects::nonNull))
                    .map(stream -> stream.filter(pair -> pair.getValue() != null && !pair.getValue().isBlank()))
                    .map(stream -> stream.map(Pair::toString))
                    .map(stream -> stream.collect(joining(", ")));
    }

    public BooleanProperty cancelFetchProperty() {
        return cancelFetch;
    }

    @FXML
    private void cancelFetch(ActionEvent actionEvent) {
        cancelFetch.setValue(null);
        cancelFetch.set(true);
        runLater(() -> artResultsTable.requestFocus());
        actionEvent.consume();
    }

}