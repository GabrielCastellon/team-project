package data_access;

import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;

import entity.Photo;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

class UnsplashCatalogTest {

    private HttpServer server;

    private URI base;

    private int responseStatus = 200;

    private String body;

    private final AtomicReference<String> request = new AtomicReference<>();

    private final AtomicReference<String> auth = new AtomicReference<>();

    @BeforeEach
    void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        base = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/");
        body =
                "{\"results\":[{\"id\":\"one\",\"description\":null,\"alt_description\":\"Bedroom\","
                    + "\"urls\":{\"small\":\"https://images.unsplash.com/test\"},"
                    + "\"links\":{\"html\":\"https://unsplash.com/photos/one\",\"download_location\":\""
                        + base
                        + "photos/one/download\"},"
                        + "\"user\":{\"name\":\"Photographer\",\"links\":{\"html\":\"https://unsplash.com/@artist\"}}}]}";
        server.createContext(
                "/",
                exchange -> {
                    request.set(exchange.getRequestURI().toString());
                    auth.set(exchange.getRequestHeaders().getFirst("Authorization"));
                    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(responseStatus, bytes.length);
                    try (var out = exchange.getResponseBody()) {
                        out.write(bytes);
                    }
                });
        server.start();
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    UnsplashCatalog catalog() {
        return new UnsplashCatalog(HttpClient.newHttpClient(), "test-key", base);
    }

    @Test
    void searchEncodesQueryMapsMetadataAndSupportsTracking() throws Exception {
        UnsplashCatalog catalog = catalog();
        Photo photo = catalog.search("bedroom & plants", 2, "portrait", "latest").get(0);
        assertTrue(request.get().contains("query=bedroom+%26+plants"));
        assertTrue(request.get().contains("page=2"));
        assertTrue(request.get().contains("orientation=portrait"));
        assertEquals("Client-ID test-key", auth.get());
        assertEquals("Bedroom", photo.description());
        assertTrue(photo.photographerUrl().contains("utm_source=team_project"));
        catalog.trackSelection(photo);
        assertEquals("/photos/one/download", request.get());
    }

    @Test
    void errorsAreActionableAndInvalidJsonIsRejected() {
        responseStatus = 429;
        assertTrue(
                assertThrows(IOException.class, () -> catalog().search("x", 1, "", "relevant"))
                        .getMessage()
                        .contains("limit"));
        responseStatus = 401;
        assertTrue(
                assertThrows(IOException.class, () -> catalog().search("x", 1, "", "relevant"))
                        .getMessage()
                        .contains("key"));
        responseStatus = 200;
        body = "not-json";
        assertThrows(IOException.class, () -> catalog().search("x", 1, "", "relevant"));
    }

    @Test
    void neverSendsCredentialsToForeignDownloadHost() {
        Photo malicious =
                new Photo(
                        "x",
                        "x",
                        "image",
                        "page",
                        "artist",
                        "profile",
                        "https://example.com/photos/x/download");
        assertThrows(IOException.class, () -> catalog().trackSelection(malicious));
        assertNull(request.get());
    }
}
