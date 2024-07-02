package com.github.idelstak.metadata.views;

import com.dlsc.gemsfx.*;
import com.github.idelstak.metadata.components.*;
import com.github.idelstak.metadata.model.*;
import com.github.idelstak.metadata.service.*;
import javafx.beans.property.*;
import javafx.beans.value.*;
import javafx.collections.ListChangeListener.*;
import javafx.collections.*;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.control.TableView.*;
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
    private final ObjectProperty<MetadataQuery> query;
    private final ObservableList<TaggedAudioFile> tagResults;
    private final ObjectProperty<TaggedAudioFile> taggedAudioFile;
    private final ObjectProperty<TaggedAudioFile> selectedTaggedAudioFile;
    private final BooleanProperty cancelFetch;
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
    private Button cancelFetchButton;
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

    public MetadataFetchViewController() {
        query = new SimpleObjectProperty<>();
        tagResults = observableArrayList();
        taggedAudioFile = new SimpleObjectProperty<>();
        selectedTaggedAudioFile = new SimpleObjectProperty<>();
        cancelFetch = new SimpleBooleanProperty();
    }

    public void setQuery(MetadataQuery query) {
        this.query.set(query);
    }

    public void setTaggedAudioFile(TaggedAudioFile taggedAudioFile) {
        this.taggedAudioFile.set(taggedAudioFile);
    }

    public TaggedAudioFile selectedTaggedAudioFile() {
        return selectedTaggedAudioFile.get();
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

    private ObservableValue<String> toQueryText() {
        return query.map(metadataQuery -> Stream.of(metadataQuery.title(),
                                                    metadataQuery.artist(),
                                                    metadataQuery.album()))
                    .map(stream -> stream.filter(Objects::nonNull))
                    .map(stream -> stream.filter(pair -> pair.getValue() != null && !pair.getValue().isBlank()))
                    .map(stream -> stream.map(Pair::toString))
                    .map(stream -> stream.collect(joining(", ")));
    }

    @FXML
    private void cancelFetch(ActionEvent actionEvent) {
        cancelFetch.set(true);
    }

}