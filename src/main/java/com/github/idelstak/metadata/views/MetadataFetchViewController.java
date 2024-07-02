package com.github.idelstak.metadata.views;

import com.dlsc.gemsfx.*;
import javafx.beans.property.*;
import javafx.beans.value.*;
import javafx.collections.ListChangeListener.*;
import javafx.collections.*;
import javafx.concurrent.*;
import javafx.fxml.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.TableView.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.util.*;
import okhttp3.*;
import org.json.*;
import org.slf4j.*;

import java.io.*;
import java.util.*;
import java.util.stream.*;

import static java.util.stream.Collectors.*;
import static javafx.application.Platform.*;
import static javafx.collections.FXCollections.*;

public class MetadataFetchViewController extends FxmlController {

    private static final String DEEZER_BAZE_URL = "https://api.deezer.com";
    private static final Logger LOG = LoggerFactory.getLogger(MetadataFetchViewController.class);
    private static final ToggleGroup TAG_RESULT_TOGGLE_GROUP = new ToggleGroup();
    private final ObjectProperty<MetadataQuery> query;
    private final ObservableList<TaggedAudioFile> tagResults;
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
    private TableColumn<String, String> tagFieldsColumn;
    @FXML
    private TableColumn originalFieldsColumn;
    @FXML
    private TableColumn fetchedFieldsColumn;
    @FXML
    private TableView tagsComparisonTable;
    @FXML
    private ProgressBar fetchProgressBar;

    public MetadataFetchViewController() {
        query = new SimpleObjectProperty<>();
        tagResults = observableArrayList();
    }

    @Override
    protected void initialize() {
        finalResultsFetchedLabel.visibleProperty().bind(fetchProgressBox.visibleProperty().not());
        artResultsTable.getSelectionModel().cellSelectionEnabledProperty().set(true);

        queryLabel.textProperty().bind(toQueryText());
        query.addListener((_, _, query) -> {
            if (query == null) {
                return;
            }

            FetchService service = new FetchService(query);
            finalResultsFetchedLabel.textProperty()
                                    .bind(service.totalWorkProperty()
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

                var service = new ColumnsPopulateService(change);
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

    void setQuery(MetadataQuery query) {
        this.query.set(query);
    }

    private static Image downloadImage(String bigCoverUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(bigCoverUrl).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code: " + response.code());
            }

            ResponseBody body = response.body();
            if (body == null) {
                LOG.error("No response body found for: {}", bigCoverUrl);
                return null;
            }
            byte[] data = body.bytes();
            return new Image(new ByteArrayInputStream(data));
        }
    }

    private static class FetchService extends Service<List<TaggedAudioFile>> {

        private final MetadataQuery query;

        private FetchService(MetadataQuery query) {this.query = query;}

        @Override
        protected Task<List<TaggedAudioFile>> createTask() {
            return new Task<>() {
                @Override
                protected List<TaggedAudioFile> call() throws Exception {
                    List<TaggedAudioFile> taggedAudioFiles = new ArrayList<>();
                    runLater(() -> updateProgress(-1, -1));

                    HttpUrl httpUrl = HttpUrl.parse(DEEZER_BAZE_URL);
                    if (httpUrl == null) {
                        throw new IOException("Couldn't parse " + DEEZER_BAZE_URL);
                    }

                    HttpUrl.Builder builder = httpUrl.newBuilder();
                    builder.addPathSegment("search");
                    builder.addQueryParameter("q", parameters(query));

                    httpUrl = builder.build();
                    Request request = new Request.Builder().url(httpUrl).build();
                    OkHttpClient client = new OkHttpClient();

                    LOG.info("About to request: {}", request);

                    try (Response response = client.newCall(request).execute()) {
                        JSONArray data = responseArray(response);
                        int dataLength = data.length();

                        for (int i = 0; i < dataLength; i++) {
                            int j = i;
                            JSONObject dataObject = data.getJSONObject(j);
                            String title = dataObject.getString("title");
                            runLater(() -> updateMessage("Fetching %s...".formatted(title)));
                            JSONObject artistObject = dataObject.getJSONObject("artist");
                            String artist = artistObject.getString("name");
                            JSONObject albumObject = dataObject.getJSONObject("album");
                            String album = albumObject.getString("title");
                            String bigCoverUrl = albumObject.getString("cover_big");
                            Image bigCover = null;

                            if (bigCoverUrl != null) {
                                bigCover = downloadImage(bigCoverUrl);
                            }

                            TaggedAudioFile file = taggedAudioFile(title, artist, album, bigCover);
                            taggedAudioFiles.add(file);
                            runLater(() -> {
                                updateProgress(j + 1, dataLength);
                                updateValue(List.of(file));
                            });
                        }
                    }
                    return taggedAudioFiles;
                }

                private static String parameters(MetadataQuery query) {
                    StringJoiner parameters = new StringJoiner(" ", "\"", "\"");
                    Pair<String, String> queryPair = query.title();
                    if (queryPair != null) {
                        //parameters.add("track:\"%s\"".formatted(queryPair.getValue()));
                        parameters.add(queryPair.getValue());
                    }
                    queryPair = query.artist();
                    if (queryPair != null) {
                        //parameters.add("artist:\"%s\"".formatted(queryPair.getValue()));
                        parameters.add(queryPair.getValue());
                    }
                    /*queryPair = query.album();
                    if (queryPair != null) {
                        //parameters.add("album:\"%s\"".formatted(queryPair.getValue()));
                        parameters.add(queryPair.getValue());
                    }*/
                    return parameters.toString();
                }
            };
        }

        private static JSONArray responseArray(Response response) throws IOException {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response.code());
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("Response body wasn't found");
            }

            JSONObject result = new JSONObject(body.string());
            return result.getJSONArray("data");
        }

        private static TaggedAudioFile taggedAudioFile(String title, String artist, String album, Image bigCover) {
            return new TaggedAudioFile(title, artist, album, null, null, bigCover, null);
        }
    }

    private static class ColumnsPopulateService extends Service<List<TableColumn<List<TaggedAudioFile>,
            TaggedAudioFile>>> {

        private final Change<? extends TaggedAudioFile> change;

        public ColumnsPopulateService(Change<? extends TaggedAudioFile> change) {this.change = change;}

        @Override
        protected Task<List<TableColumn<List<TaggedAudioFile>, TaggedAudioFile>>> createTask() {
            return new Task<>() {
                @Override
                protected List<TableColumn<List<TaggedAudioFile>, TaggedAudioFile>> call() throws Exception {
                    var columns = new ArrayList<TableColumn<List<TaggedAudioFile>, TaggedAudioFile>>();

                    for (int j = 0; j < change.getList().size(); j++) {
                        int i = j;
                        var column = new TableColumn<List<TaggedAudioFile>, TaggedAudioFile>();
                        column.setCellValueFactory(f -> {
                            return new SimpleObjectProperty<>(f.getValue().get(i));
                        });
                        column.setCellFactory(_ -> new TaggedAudioFileCell());
                        runLater(() -> updateValue(List.of(column)));
                        columns.add(column);
                    }

                    return columns;
                }
            };
        }
    }

    private static class TaggedAudioFileCell extends TableCell<List<TaggedAudioFile>, TaggedAudioFile> {

        private final StackPane root = new StackPane();
        private final AvatarView view = new AvatarView();

        {
            view.setSize(160.0f);
            HBox box = new HBox();
            box.getStyleClass().add("result-box");
            box.setAlignment(Pos.CENTER);
            box.getChildren().add(view);

            RadioButton toggle = new RadioButton();
            toggle.setToggleGroup(TAG_RESULT_TOGGLE_GROUP);
            root.getChildren().addAll(toggle, box);

            view.setOnMouseClicked(event -> {
                TableViewSelectionModel<List<TaggedAudioFile>> model = getTableView().getSelectionModel();
                model.clearAndSelect(0, getTableColumn());
            });
        }

        @Override
        protected void updateItem(TaggedAudioFile taggedAudioFile, boolean empty) {
            super.updateItem(taggedAudioFile, empty);

            if (empty || taggedAudioFile == null) {
                setGraphic(null);
            } else {
                runLater(() -> view.setImage(taggedAudioFile.art()));
                setGraphic(root);
            }
        }
    }
}