package evo.developers.ru.jonio.client.core.helpers.httpclient;

import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class TrackerSDK {
    private final HttpClient client;
    private final Gson gson;

    public TrackerSDK() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
    }


    public String get(String url, Map<String, String> headers) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(new URI(url))
                .GET();

        if (headers != null) {
            headers.forEach(requestBuilder::header);
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public String post(String url, Object body, Map<String, String> headers) throws Exception {
        String jsonBody = gson.toJson(body);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(new URI(url))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json");

        if (headers != null) {
            headers.forEach(requestBuilder::header);
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public <T> T post(String url, Object body, Map<String, String> headers, Class<T> responseClass) throws Exception {
        String response = post(url, body, headers);
        return gson.fromJson(response, responseClass);
    }
}
