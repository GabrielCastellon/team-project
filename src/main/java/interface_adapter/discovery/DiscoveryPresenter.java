package interface_adapter.discovery;

import entity.Photo;
import entity.ScoredPhoto;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/** Converts use-case results to UI cards; the view boundary belongs to this adapter layer. */
public final class DiscoveryPresenter {
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

    private final ThumbnailLoader thumbnails;
    private final View view;

    public DiscoveryPresenter(ThumbnailLoader thumbnails, View view) {
        this.thumbnails = thumbnails;
        this.view = view;
    }

    public void busy(boolean value) { view.busy(value); }
    public void status(String message) { view.status(message); }
    public void show(List<Card> cards, String message) { view.show(cards, message); }

    public List<Card> scored(List<ScoredPhoto> results) {
        List<Photo> photos = new ArrayList<>();
        List<String> scores = new ArrayList<>();
        for (ScoredPhoto result : results) {
            photos.add(result.photo());
            scores.add(String.format(java.util.Locale.ROOT, "Cosine: %.3f", result.score()));
        }
        return cards(photos, scores);
    }

    public List<Card> cards(List<Photo> photos, List<String> scores) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < photos.size(); i++) {
            BufferedImage thumbnail = null;
            try {
                thumbnail = thumbnails.load(photos.get(i).imageUrl());
            } catch (Exception ex) {
                // Keep attribution and actions available when only the preview fails.
            }
            cards.add(new Card(photos.get(i), thumbnail, scores == null ? "" : scores.get(i)));
        }
        return cards;
    }

}
