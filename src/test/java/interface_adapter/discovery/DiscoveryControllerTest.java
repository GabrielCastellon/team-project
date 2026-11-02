package interface_adapter.discovery;

import static org.junit.jupiter.api.Assertions.*;

import entity.FeatureVector;
import entity.Photo;

import org.junit.jupiter.api.Test;

import use_case.discovery.*;

import view.DiscoveryView;

import java.io.IOException;
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
        AtomicReference<List<DiscoveryController.Card>> rendered = new AtomicReference<>();
        DiscoveryController.View view =
                new DiscoveryController.View() {

                    public void busy(boolean value) {
                        updatesOnEdt.compareAndSet(true, SwingUtilities.isEventDispatchThread());
                        if (!value) {
                            done.countDown();
                        }
                    }

                    public void show(List<DiscoveryController.Card> cards, String message) {
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
                        view);
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
        DiscoveryController.View view =
                new DiscoveryController.View() {

                    public void busy(boolean value) {
                        if (!value) {
                            done.countDown();
                        }
                    }

                    public void show(List<DiscoveryController.Card> cards, String message) {
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
                        view);
        SwingUtilities.invokeAndWait(() -> controller.search("room", 1, "", "relevant"));
        assertTrue(done.await(5, TimeUnit.SECONDS));
        assertTrue(status.get().contains("rate limit"));
    }

    @Test
    void swingPanelConstructsAndRendersHeadlessly() throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    DiscoveryView view = new DiscoveryView("test-model");
                    view.show(
                            List.of(
                                    new DiscoveryController.Card(
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
