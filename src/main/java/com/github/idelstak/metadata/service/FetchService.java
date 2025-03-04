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
package com.github.idelstak.metadata.service;

import com.github.idelstak.metadata.model.*;
import javafx.concurrent.*;
import javafx.scene.image.*;
import javafx.util.*;
import okhttp3.*;
import org.json.*;

import java.io.*;
import java.util.*;

import static javafx.application.Platform.*;

public class FetchService extends Service<List<TaggedAudioFile>> {

    private static final String DEEZER_BAZE_URL = "https://api.deezer.com";
    private final MetadataQuery query;

    public FetchService(MetadataQuery query) {
        this.query = query;
    }

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

                try (Response response = client.newCall(request).execute()) {
                    JSONArray data = responseArray(response);
                    int dataLength = data.length();

                    for (int i = 0; i < dataLength; i++) {
                        if (isCancelled()) {
                            runLater(() -> updateMessage("Fetch cancelled"));
                            break;
                        }
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
                    parameters.add(queryPair.getValue());
                }
                queryPair = query.artist();
                if (queryPair != null) {
                    parameters.add(queryPair.getValue());
                }
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

    private static Image downloadImage(String bigCoverUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(bigCoverUrl).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code: " + response.code());
            }

            ResponseBody body = response.body();
            if (body == null) {
                return null;
            }
            byte[] data = body.bytes();
            return new Image(new ByteArrayInputStream(data));
        }
    }

    private static TaggedAudioFile taggedAudioFile(String title, String artist, String album, Image bigCover) {
        return new TaggedAudioFile(title, artist, album, bigCover, null);
    }
}