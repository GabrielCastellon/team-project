package data_access;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public final class ImageLoader {

    private final HttpClient client =
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    private final Map<String, BufferedImage> cache =
            new LinkedHashMap<String, BufferedImage>(32, .75f, true) {

                @Override
                protected boolean removeEldestEntry(Map.Entry<String, BufferedImage> entry) {
                    return size() > 80;
                }
            };

    public synchronized BufferedImage load(String url) throws IOException {
        if (cache.containsKey(url)) {
            return cache.get(url);
        }
        URI uri = URI.create(url);
        if (!"https".equals(uri.getScheme()) || !"images.unsplash.com".equals(uri.getHost())) {
            throw new IOException("Expected an Unsplash HTTPS image URL");
        }
        try {
            HttpResponse<byte[]> response =
                    client.send(
                            HttpRequest.newBuilder(uri)
                                    .timeout(Duration.ofSeconds(20))
                                    .GET()
                                    .build(),
                            HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                throw new IOException("Image unavailable (HTTP " + response.statusCode() + ")");
            }
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(response.body()));
            if (image == null) {
                throw new IOException("Unsupported image format");
            }
            cache.put(url, image);
            return image;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("Image loading cancelled", ex);
        }
    }
}
