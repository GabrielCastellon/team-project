package interface_adapter.discovery;

import entity.Photo;

import use_case.discovery.DiscoveryInputBoundary;
import interface_adapter.discovery.DiscoveryPresenter.Card;
import interface_adapter.discovery.DiscoveryPresenter.ThumbnailLoader;
import interface_adapter.discovery.DiscoveryPresenter.View;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

public final class DiscoveryController {

    private final DiscoveryInputBoundary service;

    private final DiscoveryPresenter presenter;

    private List<Photo> candidates = List.of();

    private boolean working;

    public DiscoveryController(
            DiscoveryInputBoundary service, ThumbnailLoader thumbnails, View view) {
        this.service = service;
        this.presenter = new DiscoveryPresenter(thumbnails, view);
    }

    public void search(String query, int page, String orientation, String order) {
        run(
                () -> {
                    candidates = List.of();
                    List<Photo> found = service.search(query, page, orientation, order);
                    List<Card> cards = presenter.cards(found, null);
                    candidates = found;
                    return cards;
                },
                "Search page " + page + ". Recommendations rank this page's candidates.");
    }

    public void recommend(String a) {
        run(
                () -> presenter.scored(service.recommend(requireCandidates(), a)),
                "Best matches first; saved images excluded.");
    }

    public void mix(String a, String b, double weight) {
        run(
                () -> presenter.scored(service.mix(requireCandidates(), a, b, weight)),
                "Taste Mixer: " + Math.round(100 * weight) + "% A.");
    }

    public void group(List<String> profiles) {
        run(
                () -> presenter.scored(service.group(requireCandidates(), profiles)),
                "Group: highest minimum similarity first.");
    }

    public void analyze(String a) {
        run(
                () -> presenter.scored(service.analyze(a)),
                "Board analysis: least similar first; scores are cosine similarity, not"
                        + " probabilities.");
    }

    public void samples(String a) {
        run(() -> presenter.cards(service.samples(a), null), "Saved preferences for " + a + ".");
    }

    public void like(String a, Photo photo) {
        run(
                () -> {
                    service.like(a, photo);
                    return null;
                },
                "Saved preference to " + a + ".");
    }

    public void remove(String a, Photo photo) {
        run(
                () -> {
                    service.remove(a, photo.id());
                    return presenter.cards(service.samples(a), null);
                },
                "Removed preference from " + a + ".");
    }

    private List<Photo> requireCandidates() {
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException(
                    "Search for images before requesting recommendations");
        }
        return candidates;
    }

    private void run(Callable<List<Card>> task, String message) {
        if (working) {
            return;
        }
        working = true;
        presenter.busy(true);
        presenter.status("Working…");
        new SwingWorker<List<Card>, Void>() {

            @Override
            protected List<Card> doInBackground() throws Exception {
                return task.call();
            }

            @Override
            protected void done() {
                try {
                    List<Card> result = get();
                    if (result == null) {
                        presenter.status(message);
                    } else {
                        presenter.show(result, result.isEmpty() ? "No images. " + message : message);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    presenter.status("Cancelled");
                } catch (ExecutionException ex) {
                    String detail = ex.getCause().getMessage();
                    presenter.status(
                            "Could not complete: "
                                    + (detail == null ? "unexpected error" : detail));
                } finally {
                    working = false;
                    presenter.busy(false);
                }
            }
        }.execute();
    }
}
