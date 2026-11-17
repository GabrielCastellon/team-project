package app;

import java.awt.Component;
import java.awt.Container;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import view.DiscoveryView;
import view.MainView;
import view.PlaceholderView;
import static org.junit.jupiter.api.Assertions.*;

/** Exercises the real module inside the shared shell without credentials or HTTP calls. */
class DiscoveryShellIntegrationTest {
    @TempDir
    Path directory;

    @Test
    void realDiscoveryPanelIsLazyAndRetainedInsideTheSkeleton() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            AtomicInteger initializations = new AtomicInteger();
            ApplicationFactory factory = new ApplicationFactory(() -> {
                initializations.incrementAndGet();
                return DiscoveryModule.createPanel(Map.of(
                        "UNSPLASH_ACCESS_KEY", "fixture-not-a-live-key",
                        "DISCOVERY_DATA_DIR", directory.toString()));
            });
            MainView shell = factory.createView();
            JTabbedPane tabs = (JTabbedPane) shell.getComponent(0);
            assertEquals(5, tabs.getTabCount());
            assertEquals(0, initializations.get());
            assertInstanceOf(PlaceholderView.class, content(tabs));
            tabs.setSelectedIndex(tabs.indexOfTab("Discovery"));
            JPanel discovery = content(tabs);
            assertInstanceOf(DiscoveryView.class, discovery);
            assertEquals(1, initializations.get());
            tabs.setSelectedIndex(tabs.indexOfTab("Boards"));
            assertInstanceOf(PlaceholderView.class, content(tabs));
            tabs.setSelectedIndex(tabs.indexOfTab("Discovery"));
            assertSame(discovery, content(tabs));
            assertEquals(1, initializations.get());
        });
    }

    @Test
    void missingDiscoveryConfigurationDoesNotBreakOtherTeamFeatures() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MainView shell = new ApplicationFactory(
                    () -> DiscoveryModule.createPanel(Map.of())).createView();
            JTabbedPane tabs = (JTabbedPane) shell.getComponent(0);
            tabs.setSelectedIndex(tabs.indexOfTab("Discovery"));
            assertTrue(containsLabel((Container) tabs.getSelectedComponent(), "UNSPLASH_ACCESS_KEY"));
            tabs.setSelectedIndex(tabs.indexOfTab("Accounts"));
            assertInstanceOf(PlaceholderView.class, content(tabs));
            tabs.setSelectedIndex(tabs.indexOfTab("Pins"));
            assertInstanceOf(PlaceholderView.class, content(tabs));
        });
    }

    private static JPanel content(JTabbedPane tabs) {
        return (JPanel) ((JPanel) tabs.getSelectedComponent()).getComponent(0);
    }

    private static boolean containsLabel(Container container, String text) {
        for (Component component : container.getComponents()) {
            if (component instanceof JLabel label && label.getText().contains(text)) {
                return true;
            }
            if (component instanceof Container child && containsLabel(child, text)) {
                return true;
            }
        }
        return false;
    }
}
