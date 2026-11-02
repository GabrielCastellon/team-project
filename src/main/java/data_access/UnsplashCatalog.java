package data_access;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import entity.Photo;

import use_case.discovery.ImageCatalog;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public final class UnsplashCatalog implements ImageCatalog {

    private final HttpClient client;

    private final String accessKey;

    private final URI base;

    public UnsplashCatalog(String accessKey) {
        this(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(),
                accessKey,
                URI.create("https://api.unsplash.com/"));
    }

    public UnsplashCatalog(HttpClient client, String accessKey, URI base) {
        if (accessKey == null || accessKey.isBlank()) {
            throw new IllegalArgumentException("Set UNSPLASH_ACCESS_KEY before starting");
        }
        this.client = client;
        this.accessKey = accessKey;
        this.base = base;
    }

    @Override
    public List<Photo> search(String query, int page, String orientation, String order)
            throws IOException {
        String path =
                "search/photos?query="
                        + URLEncoder.encode(query, StandardCharsets.UTF_8)
                        + "&page="
                        + page
                        + "&per_page=20&order_by="
                        + order
                        + (orientation.isEmpty() ? "" : "&orientation=" + orientation);
        try {
            JsonObject root = JsonParser.parseString(get(base.resolve(path))).getAsJsonObject();
            List<Photo> photos = new ArrayList<>();
            for (JsonElement element : root.getAsJsonArray("results")) {
                JsonObject p = element.getAsJsonObject();
                JsonObject user = p.getAsJsonObject("user"), links = p.getAsJsonObject("links");
                String description =
                        optional(
                                p, "description", optional(p, "alt_description", "Untitled photo"));
                photos.add(
                        new Photo(
                                p.get("id").getAsString(),
                                description,
                                p.getAsJsonObject("urls").get("small").getAsString(),
                                attribution(links.get("html").getAsString()),
                                user.get("name").getAsString(),
                                attribution(
                                        user.getAsJsonObject("links").get("html").getAsString()),
                                links.get("download_location").getAsString()));
            }
            return photos;
        } catch (RuntimeException ex) {
            throw new IOException("Unsplash returned an invalid response", ex);
        }
    }

    @Override
    public void trackSelection(Photo photo) throws IOException {
        URI uri = URI.create(photo.downloadUrl());
        if (!base.getScheme().equals(uri.getScheme())
                || !base.getAuthority().equals(uri.getAuthority())
                || !uri.getPath().startsWith("/photos/")
                || !uri.getPath().endsWith("/download")) {
            throw new IOException("Invalid Unsplash download tracking URL");
        }
        get(uri);
    }

    private String get(URI uri) throws IOException {
        HttpRequest request =
                HttpRequest.newBuilder(uri)
                        .timeout(Duration.ofSeconds(20))
                        .header("Authorization", "Client-ID " + accessKey)
                        .header("Accept-Version", "v1")
                        .GET()
                        .build();
        try {
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status == 401) {
                throw new IOException("Unsplash rejected the access key");
            }
            if (status == 403 || status == 429) {
                throw new IOException("Unsplash access/rate limit reached; try again later");
            }
            if (status != 200) {
                throw new IOException("Unsplash request failed (HTTP " + status + ")");
            }
            return response.body();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("Request cancelled", ex);
        }
    }

    private static String optional(JsonObject object, String key, String fallback) {
        return !object.has(key) || object.get(key).isJsonNull()
                ? fallback
                : object.get(key).getAsString();
    }

    private static String attribution(String url) {
        return url
                + (url.contains("?") ? "&" : "?")
                + "utm_source=team_project&utm_medium=referral";
    }
}
