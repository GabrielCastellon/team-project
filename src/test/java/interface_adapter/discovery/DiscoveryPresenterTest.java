package interface_adapter.discovery;

import entity.Photo;
import entity.ScoredPhoto;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DiscoveryPresenterTest {
    @Test
    void unavailablePreviewRetainsMetadataAndScoresUseStableFormatting() {
        Photo missing = new Photo("a", "Missing", "bad", "page-a", "Artist A", "profile-a", "track-a");
        Photo available = new Photo("b", "Available", "good", "page-b", "Artist B", "profile-b", "track-b");
        BufferedImage thumbnail = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        DiscoveryPresenter presenter = new DiscoveryPresenter(url -> {
            if (url.equals("bad")) {
                throw new IOException("Preview unavailable");
            }
            return thumbnail;
        }, new DiscoveryPresenter.View() {
            public void busy(boolean value) { }
            public void show(List<DiscoveryPresenter.Card> cards, String message) { }
            public void status(String message) { }
        });
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);
            var cards = presenter.scored(List.of(
                    new ScoredPhoto(missing, .5), new ScoredPhoto(available, -.25)));
            assertEquals(2, cards.size());
            assertSame(missing, cards.get(0).photo);
            assertNull(cards.get(0).thumbnail);
            assertEquals("Cosine: 0.500", cards.get(0).score);
            assertSame(thumbnail, cards.get(1).thumbnail);
            assertEquals("Cosine: -0.250", cards.get(1).score);
        } finally {
            Locale.setDefault(previous);
        }
    }
}
