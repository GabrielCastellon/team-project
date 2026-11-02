package use_case.discovery;

import static org.junit.jupiter.api.Assertions.*;

import entity.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

class DiscoveryServiceTest {

    private final Map<String, List<Photo>> saved = new HashMap<>();

    private final Map<String, FeatureVector> vectors = new HashMap<>();

    private DiscoveryService service;

    private int tracked;

    private boolean failTracking;

    static Photo photo(String id) {
        return new Photo(id, id, "image:" + id, "page", "artist", "artist-page", "download");
    }

    @BeforeEach
    void setup() {
        service =
                new DiscoveryService(
                        new ImageCatalog() {

                            public List<Photo> search(
                                    String query, int page, String orientation, String order) {
                                return List.of(photo("x"));
                            }

                            public void trackSelection(Photo photo) throws IOException {
                                if (failTracking) {
                                    throw new IOException("tracking failed");
                                }
                                tracked++;
                            }
                        },
                        new EmbeddingProvider() {

                            public String modelId() {
                                return "test";
                            }

                            public FeatureVector embed(Photo photo) throws IOException {
                                if (!vectors.containsKey(photo.id())) {
                                    throw new IOException("embedding unavailable");
                                }
                                return vectors.get(photo.id());
                            }
                        },
                        new PreferenceStore() {

                            public List<Photo> load(String id) {
                                return saved.getOrDefault(id, List.of());
                            }

                            public void save(String id, List<Photo> photos) {
                                saved.put(id, List.copyOf(photos));
                            }
                        });
        vector("a", 1, 0);
        vector("b", 0, 1);
        vector("x", 1, 0);
        vector("y", 0, 1);
        vector("z", 1, 1);
        saved.put("A", List.of(photo("a")));
        saved.put("B", List.of(photo("b")));
    }

    void vector(String id, double x, double y) {
        vectors.put(id, new FeatureVector(new double[] {x, y}));
    }

    List<String> ids(List<ScoredPhoto> photos) {
        return photos.stream().map(p -> p.photo().id()).collect(Collectors.toList());
    }

    @Test
    void personalRankingExcludesSavedDeduplicatesAndSorts() throws Exception {
        assertEquals(
                List.of("x", "z", "y"),
                ids(
                        service.recommend(
                                List.of(photo("y"), photo("a"), photo("x"), photo("z"), photo("x")),
                                "A")));
    }

    @Test
    void mixerChangesRankingAndEndpointsAllowEmptyInactiveBoard() throws Exception {
        List<Photo> candidates = List.of(photo("x"), photo("y"));
        assertEquals("x", service.mix(candidates, "A", "B", .7).get(0).photo().id());
        assertEquals("y", service.mix(candidates, "A", "B", .3).get(0).photo().id());
        assertEquals("x", service.mix(candidates, "A", "empty", 1).get(0).photo().id());
        assertEquals("y", service.mix(candidates, "empty", "B", 0).get(0).photo().id());
    }

    @Test
    void groupPrefersCompromiseAndDeterministicTies() throws Exception {
        assertEquals(
                List.of("z", "x", "y"),
                ids(service.group(List.of(photo("y"), photo("x"), photo("z")), List.of("A", "B"))));
    }

    @Test
    void analyzerRanksOutlierFirst() throws Exception {
        saved.put("board", List.of(photo("a"), photo("x"), photo("b")));
        assertEquals("b", service.analyze("board").get(0).photo().id());
        assertEquals(3, service.samples("board").size());
    }

    @Test
    void likeIsIdempotentAndRemovePersists() throws Exception {
        service.like(" A ", photo("z"));
        service.like("A", photo("z"));
        assertEquals(2, service.samples("A").size());
        assertEquals(1, tracked);
        service.remove("A", "z");
        assertEquals(1, service.samples("A").size());
    }

    @Test
    void failedEmbeddingOrTrackingDoesNotSave() {
        assertThrows(IOException.class, () -> service.like("A", photo("missing")));
        failTracking = true;
        assertThrows(IOException.class, () -> service.like("A", photo("z")));
        assertEquals(1, saved.get("A").size());
    }

    @Test
    void validatesSearchProfilesAndEmptyGroups() {
        assertThrows(IllegalArgumentException.class, () -> service.search(" ", 1, "", "relevant"));
        assertThrows(IllegalArgumentException.class, () -> service.search("x", 0, "", "relevant"));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.search("x", 1, "diagonal", "relevant"));
        assertThrows(IllegalArgumentException.class, () -> service.search("x", 1, "", "popular"));
        assertThrows(IllegalArgumentException.class, () -> service.preference("empty"));
        assertThrows(IllegalArgumentException.class, () -> service.samples(" "));
        assertThrows(IllegalArgumentException.class, () -> service.analyze("A"));
        assertThrows(IllegalArgumentException.class, () -> service.group(List.of(), List.of()));
    }
}
