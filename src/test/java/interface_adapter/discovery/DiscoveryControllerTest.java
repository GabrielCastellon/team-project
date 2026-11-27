package interface_adapter.discovery;

import static org.junit.jupiter.api.Assertions.*;

import data_access.JsonPinDataAccessObject;
import entity.FeatureVector;
import entity.Photo;
import entity.Pin;

import interface_adapter.save_pin.SavePinViewModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import use_case.discovery.*;

import view.DiscoveryView;
import view.SwingTaskRunner;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;

class DiscoveryControllerTest {

    @Test
    void networkRunsOffEdtAndViewUpdatesOnEdt() throws Exception {
        CountDownLatch done = new CountDownLatch(1);
        AtomicBoolean networkOnEdt = new AtomicBoolean(true);
        AtomicBoolean updatesOnEdt = new AtomicBoolean(true);
        AtomicReference<List<DiscoveryPresenter.Card>> rendered = new AtomicReference<>();
        DiscoveryPresenter.View view =
                new DiscoveryPresenter.View() {

                    public void busy(boolean value) {
                        updatesOnEdt.compareAndSet(true, SwingUtilities.isEventDispatchThread());
                        if (!value) {
                            done.countDown();
                        }
                    }

                    public void show(List<DiscoveryPresenter.Card> cards, String message) {
                        updatesOnEdt.compareAndSet(true, SwingUtilities.isEventDispatchThread());
                        rendered.set(cards);
                    }

                    public void status(String message) {
                        updatesOnEdt.compareAndSet(true, SwingUtilities.isEventDispatchThread());
                    }
                };
        DiscoveryController controller =
                new DiscoveryController(
                        service(
                                () -> {
                                    networkOnEdt.set(SwingUtilities.isEventDispatchThread());
                                    return List.of(
                                            new Photo(
                                                    "x",
                                                    "photo",
                                                    "image",
                                                    "page",
                                                    "artist",
                                                    "profile",
                                                    "download"));
                                }),
                        url -> {
                            throw new IOException("thumbnail unavailable");
                        },
                        view,
                        new SwingTaskRunner<>());
        SwingUtilities.invokeAndWait(() -> controller.search("room", 1, "", "relevant"));
        assertTrue(done.await(5, TimeUnit.SECONDS));
        assertFalse(networkOnEdt.get());
        assertTrue(updatesOnEdt.get());
        assertEquals(1, rendered.get().size());
        assertNull(rendered.get().get(0).thumbnail);
    }

    @Test
    void failureReenablesViewAndShowsError() throws Exception {
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<String> status = new AtomicReference<>();
        DiscoveryPresenter.View view =
                new DiscoveryPresenter.View() {

                    public void busy(boolean value) {
                        if (!value) {
                            done.countDown();
                        }
                    }

                    public void show(List<DiscoveryPresenter.Card> cards, String message) {
                        fail("No success expected");
                    }

                    public void status(String message) {
                        status.set(message);
                    }
                };
        DiscoveryController controller =
                new DiscoveryController(
                        service(
                                () -> {
                                    throw new IOException("rate limit");
                                }),
                        url -> null,
                        view,
                        new SwingTaskRunner<>());
        SwingUtilities.invokeAndWait(() -> controller.search("room", 1, "", "relevant"));
        assertTrue(done.await(5, TimeUnit.SECONDS));
        assertTrue(status.get().contains("rate limit"));
    }

    @Test
    void failedSearchCannotReusePreviousCandidatesAndLaterSearchRecovers() throws Exception {
        AtomicBoolean failSearch = new AtomicBoolean(false);
        AtomicReference<CountDownLatch> done = new AtomicReference<>();
        AtomicReference<String> status = new AtomicReference<>();
        DiscoveryPresenter.View view =
                new DiscoveryPresenter.View() {
                    public void busy(boolean value) {
                        if (!value) {
                            done.get().countDown();
                        }
                    }

                    public void show(List<DiscoveryPresenter.Card> cards, String message) {
                        status.set(message);
                    }

                    public void status(String message) {
                        status.set(message);
                    }
                };
        DiscoveryController controller =
                new DiscoveryController(
                        service(
                                () -> {
                                    if (failSearch.get()) {
                                        throw new IOException("search unavailable");
                                    }
                                    return List.of(
                                            new Photo(
                                                    "x",
                                                    "photo",
                                                    "image",
                                                    "page",
                                                    "artist",
                                                    "profile",
                                                    "download"));
                                }),
                        url -> null,
                        view,
                        new SwingTaskRunner<>());

        runAndWait(done, () -> controller.recommend("A"));
        assertTrue(status.get().contains("Search for images"));
        runAndWait(done, () -> controller.search("room", 1, "", "relevant"));
        assertTrue(status.get().contains("Search page 1"));
        failSearch.set(true);
        runAndWait(done, () -> controller.search("forest", 1, "", "relevant"));
        assertTrue(status.get().contains("search unavailable"));
        runAndWait(done, () -> controller.recommend("A"));
        assertTrue(status.get().contains("Search for images"));
        runAndWait(done, () -> controller.mix("A", "B", .5));
        assertTrue(status.get().contains("Search for images"));
        runAndWait(done, () -> controller.group(List.of("A", "B")));
        assertTrue(status.get().contains("Search for images"));
        failSearch.set(false);
        runAndWait(done, () -> controller.search("forest", 1, "", "relevant"));
        runAndWait(done, () -> controller.recommend("A"));
        assertTrue(status.get().contains("Save at least one preference image"));
    }

    @Test
    void savedPinRecommendationsUseTheActiveAccount(@TempDir Path directory) throws Exception {
        Photo first = new Photo("a", "First", "image-a", "page-a", "Artist", "profile-a", "download-a");
        Photo second = new Photo("b", "Second", "image-b", "page-b", "Artist", "profile-b", "download-b");
        Path file = directory.resolve("pins.json");
        JsonPinDataAccessObject writer = new JsonPinDataAccessObject(file);
        writer.save(new Pin("pin-a", "alice", first));
        writer.save(new Pin("pin-b", "bob", second));
        JsonPinDataAccessObject savedPins = new JsonPinDataAccessObject(file);

        AtomicReference<String> owner = new AtomicReference<>();
        AtomicReference<CountDownLatch> done = new AtomicReference<>();
        AtomicReference<List<String>> shown = new AtomicReference<>();
        AtomicReference<String> status = new AtomicReference<>();
        DiscoveryPresenter.View view = new DiscoveryPresenter.View() {
            public void busy(boolean value) {
                if (!value) {
                    done.get().countDown();
                }
            }

            public void show(List<DiscoveryPresenter.Card> cards, String message) {
                shown.set(cards.stream().map(card -> card.photo.id()).toList());
                status.set(message);
            }

            public void status(String message) {
                status.set(message);
            }
        };
        DiscoveryController controller = new DiscoveryController(
                service(() -> List.of(first, second)), url -> null, view, new SwingTaskRunner<>());

        runAndWait(done, () -> controller.search("room", 1, "", "relevant"));
        SwingUtilities.invokeAndWait(() -> controller.recommendFromPins(owner::get, savedPins));
        assertTrue(status.get().contains("Sign in"));

        owner.set("alice");
        runAndWait(done, () -> controller.recommendFromPins(owner::get, savedPins));
        assertEquals(List.of("b"), shown.get());

        owner.set("bob");
        runAndWait(done, () -> controller.recommendFromPins(owner::get, savedPins));
        assertEquals(List.of("a"), shown.get());

        owner.set("no-pins");
        runAndWait(done, () -> controller.recommendFromPins(owner::get, savedPins));
        assertTrue(status.get().contains("Save a pin"));

        owner.set("alice");
        runAndWait(done, () -> controller.recommendFromPins(owner::get, id -> {
            List<Pin> pins = savedPins.findByOwnerId(id);
            owner.set("bob");
            return pins;
        }));
        assertTrue(status.get().contains("Account changed"));
        assertEquals(List.of("a"), shown.get());
    }

    private void runAndWait(AtomicReference<CountDownLatch> done, Runnable action)
            throws Exception {
        done.set(new CountDownLatch(1));
        SwingUtilities.invokeAndWait(action);
        assertTrue(done.get().await(5, TimeUnit.SECONDS));
    }

    @Test
    void swingPanelConstructsAndRendersHeadlessly() throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    DiscoveryView view = new DiscoveryView("test-model",
                            new SavePinViewModel());
                    view.show(
                            List.of(
                                    new DiscoveryPresenter.Card(
                                            new Photo(
                                                    "x",
                                                    "Sample",
                                                    "image",
                                                    "page",
                                                    "Artist",
                                                    "profile",
                                                    "download"),
                                            null,
                                            "Cosine: 0.5")),
                            "Ready");
                    view.busy(true);
                    view.busy(false);
                    assertEquals(3, view.getComponentCount());
                });
    }

    interface Search {

        List<Photo> run() throws IOException;
    }

    DiscoveryService service(Search search) {
        return new DiscoveryService(
                new ImageCatalog() {

                    public List<Photo> search(String q, int page, String orientation, String order)
                            throws IOException {
                        return search.run();
                    }

                    public void trackSelection(Photo p) {}
                },
                new EmbeddingProvider() {

                    public String modelId() {
                        return "test";
                    }

                    public FeatureVector embed(Photo p) {
                        return new FeatureVector(new double[] {1});
                    }
                },
                new PreferenceStore() {

                    public List<Photo> load(String id) {
                        return List.of();
                    }

                    public void save(String id, List<Photo> photos) {}
                });
    }
}
