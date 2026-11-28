package interface_adapter.discovery;

import static org.junit.jupiter.api.Assertions.*;

import data_access.JsonPinDataAccessObject;
import entity.FeatureVector;
import entity.InMemorySessionManager;
import entity.Photo;
import entity.Pin;
import entity.User;

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
import java.util.function.Supplier;

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
        RecordingView view = new RecordingView();
        DiscoveryController controller = new DiscoveryController(
                service(() -> List.of(first, second)), url -> null, view, new SwingTaskRunner<>());

        runAndWait(view.done, () -> controller.search("room", 1, "", "relevant"));
        SwingUtilities.invokeAndWait(() -> controller.recommendFromPins(owner::get, savedPins));
        assertTrue(view.message.contains("Sign in"));

        owner.set("alice");
        runAndWait(view.done, () -> controller.recommendFromPins(owner::get, savedPins));
        assertEquals(List.of("b"), view.photoIds);

        owner.set("bob");
        runAndWait(view.done, () -> controller.recommendFromPins(owner::get, savedPins));
        assertEquals(List.of("a"), view.photoIds);

        owner.set("no-pins");
        runAndWait(view.done, () -> controller.recommendFromPins(owner::get, savedPins));
        assertTrue(view.message.contains("Save a pin"));

        owner.set("alice");
        runAndWait(view.done, () -> controller.recommendFromPins(owner::get, id -> {
            List<Pin> pins = savedPins.findByOwnerId(id);
            owner.set("bob");
            return pins;
        }));
        assertTrue(view.message.contains("Account changed"));
        assertEquals(List.of("a"), view.photoIds);
    }

    @Test
    void logoutAndAccountSwitchClearCompletedRankings() throws Exception {
        Photo first = new Photo("a", "First", "image-a", "page-a", "Artist", "profile", "download-a");
        Photo second = new Photo("b", "Second", "image-b", "page-b", "Artist", "profile", "download-b");
        User alice = new User("alice", "Alice", "hash");
        User bob = new User("bob", "Bob", "hash");
        InMemorySessionManager session = new InMemorySessionManager();
        Supplier<String> owner = () -> {
            User user = session.getCurrentUser();
            return user == null ? null : user.getId();
        };
        RecordingView view = new RecordingView();
        DiscoveryController controller = new DiscoveryController(
                service(() -> List.of(first, second)), url -> null, view, new SwingTaskRunner<>());
        session.addSessionListener(controller::sessionChanged);
        Runnable rank = () -> controller.recommendFromPins(owner, id -> {
            Photo saved = id.equals("alice") ? first : second;
            return List.of(new Pin("pin", id, saved));
        });

        runAndWait(view.done, () -> controller.search("room", 1, "", "relevant"));
        SwingUtilities.invokeAndWait(() -> session.setCurrentUser(alice));
        assertEquals(List.of("a", "b"), view.photoIds);
        runAndWait(view.done, rank);
        assertEquals(List.of("b"), view.photoIds);

        SwingUtilities.invokeAndWait(() -> session.setCurrentUser(bob));
        assertTrue(view.photoIds.isEmpty());
        runAndWait(view.done, rank);
        assertEquals(List.of("a"), view.photoIds);
        SwingUtilities.invokeAndWait(session::logout);
        assertTrue(view.photoIds.isEmpty());
        SwingUtilities.invokeAndWait(rank);
        assertTrue(view.message.contains("Sign in"));
    }

    @Test
    void signingBackIntoSameAccountCannotRestoreAnEarlierRequest() throws Exception {
        Photo saved = new Photo("a", "First", "image-a", "page-a", "Artist", "profile", "download-a");
        Photo other = new Photo("b", "Second", "image-b", "page-b", "Artist", "profile", "download-b");
        InMemorySessionManager session = new InMemorySessionManager();
        User alice = new User("alice", "Alice", "hash");
        session.setCurrentUser(alice);
        RecordingView view = new RecordingView();
        DiscoveryController controller = new DiscoveryController(
                service(() -> List.of(saved, other)), url -> null, view, new SwingTaskRunner<>());
        session.addSessionListener(controller::sessionChanged);
        runAndWait(view.done, () -> controller.search("room", 1, "", "relevant"));

        runAndWait(view.done, () -> controller.recommendFromPins(() -> session.getCurrentUser().getId(), id -> {
            SwingUtilities.invokeLater(() -> {
                session.logout();
                session.setCurrentUser(alice);
            });
            return List.of(new Pin("pin", id, saved));
        }));
        assertEquals(List.of("a", "b"), view.photoIds);
        assertTrue(view.message.contains("Account changed"));
    }

    private static final class RecordingView implements DiscoveryPresenter.View {
        private final AtomicReference<CountDownLatch> done = new AtomicReference<>();
        private List<String> photoIds = List.of();
        private String message = "";

        @Override public void busy(boolean value) {
            if (!value) {
                done.get().countDown();
            }
        }

        @Override public void show(List<DiscoveryPresenter.Card> cards, String status) {
            photoIds = cards.stream().map(card -> card.photo.id()).toList();
            message = status;
        }

        @Override public void status(String status) {
            message = status;
        }
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
