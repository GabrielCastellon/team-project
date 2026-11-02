package interface_adapter.discovery;

import entity.Photo;
import entity.ScoredPhoto;

import use_case.discovery.DiscoveryService;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

public final class DiscoveryController {

    public interface View {

        void busy(boolean value);

        void show(List<Card> cards, String message);

        void status(String message);
    }

    @FunctionalInterface
    public interface ThumbnailLoader {

        BufferedImage load(String url) throws Exception;
    }

    public static final class Card {

        public final Photo photo;

        public final BufferedImage thumbnail;

        public final String score;

        Card(Photo photo, BufferedImage thumbnail, String score) {
            this.photo = photo;
            this.thumbnail = thumbnail;
            this.score = score;
        }
    }

    private final DiscoveryService service;

    private final ThumbnailLoader thumbnails;

    private final View view;

    private List<Photo> candidates = List.of();

    private boolean working;

    public DiscoveryController(DiscoveryService service, ThumbnailLoader thumbnails, View view) {
        this.service = service;
        this.thumbnails = thumbnails;
        this.view = view;
    }

    public void search(String query, int page, String orientation, String order) {
        run(
                () -> {
                    List<Photo> found = service.search(query, page, orientation, order);
                    List<Card> cards = cards(found, null);
                    candidates = found;
                    return cards;
                },
                "Search page " + page + ". Recommendations rank this page's candidates.");
    }

    public void recommend(String a) {
        run(
                () -> scored(service.recommend(candidates, a)),
                "Best matches first; saved images excluded.");
    }

    public void mix(String a, String b, double weight) {
        run(
                () -> scored(service.mix(candidates, a, b, weight)),
                "Taste Mixer: " + Math.round(100 * weight) + "% A.");
    }

    public void group(List<String> profiles) {
        run(
                () -> scored(service.group(candidates, profiles)),
                "Group: highest minimum similarity first.");
    }

    public void analyze(String a) {
        run(
                () -> scored(service.analyze(a)),
                "Board analysis: least similar first; scores are cosine similarity, not"
                        + " probabilities.");
    }

    public void samples(String a) {
        run(() -> cards(service.samples(a), null), "Saved preferences for " + a + ".");
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
                    return cards(service.samples(a), null);
                },
                "Removed preference from " + a + ".");
    }

    private List<Card> scored(List<ScoredPhoto> results) {
        List<Photo> photos = new ArrayList<>();
        List<String> scores = new ArrayList<>();
        for (ScoredPhoto result : results) {
            photos.add(result.photo());
            scores.add(String.format(java.util.Locale.ROOT, "Cosine: %.3f", result.score()));
        }
        return cards(photos, scores);
    }

    private List<Card> cards(List<Photo> photos, List<String> scores) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < photos.size(); i++) {
            BufferedImage thumbnail = null;
            try {
                thumbnail = thumbnails.load(photos.get(i).imageUrl());
            } catch (Exception ex) {
            }
            cards.add(new Card(photos.get(i), thumbnail, scores == null ? "" : scores.get(i)));
        }
        return cards;
    }

    private void run(Callable<List<Card>> task, String message) {
        if (working) {
            return;
        }
        working = true;
        view.busy(true);
        view.status("Working…");
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
                        view.status(message);
                    } else {
                        view.show(result, result.isEmpty() ? "No images. " + message : message);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    view.status("Cancelled");
                } catch (ExecutionException ex) {
                    String detail = ex.getCause().getMessage();
                    view.status(
                            "Could not complete: "
                                    + (detail == null ? "unexpected error" : detail));
                } finally {
                    working = false;
                    view.busy(false);
                }
            }
        }.execute();
    }
}
