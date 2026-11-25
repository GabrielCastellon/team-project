package view;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MainViewTest {
    @Test
    void navigationLoadsOnlySelectedFeatureAndRetainsItsState() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            AtomicInteger firstLoads = new AtomicInteger();
            AtomicInteger secondLoads = new AtomicInteger();
            JPanel retained = new JPanel();
            Map<String, Supplier<JPanel>> factories = new LinkedHashMap<>();
            factories.put("Accounts", () -> { firstLoads.incrementAndGet(); return retained; });
            factories.put("Discovery", () -> { secondLoads.incrementAndGet(); return new JPanel(); });
            MainView view = new MainView(factories);
            JTabbedPane tabs = (JTabbedPane) view.getComponent(0);
            assertEquals(1, firstLoads.get());
            assertEquals(0, secondLoads.get());
            tabs.setSelectedIndex(1);
            tabs.setSelectedIndex(0);
            assertEquals(1, firstLoads.get());
            assertEquals(1, secondLoads.get());
            assertSame(retained, ((JPanel) tabs.getSelectedComponent()).getComponent(0));
        });
    }

    @Test
    void failedFeatureDoesNotPreventNavigationAndCanBeRetried() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            AtomicInteger attempts = new AtomicInteger();
            Map<String, Supplier<JPanel>> factories = new LinkedHashMap<>();
            factories.put("Broken", () -> {
                if (attempts.incrementAndGet() == 1) {
                    throw new IllegalArgumentException("Missing configuration");
                }
                return new JPanel();
            });
            factories.put("Working", JPanel::new);
            MainView view = new MainView(factories);
            JTabbedPane tabs = (JTabbedPane) view.getComponent(0);
            tabs.setSelectedIndex(1);
            assertEquals(1, attempts.get());
            tabs.setSelectedIndex(0);
            assertEquals(2, attempts.get());
            assertEquals(1, ((JPanel) tabs.getSelectedComponent()).getComponentCount());
        });
    }

    @Test
    void activationRejectsBackgroundThread() {
        LazyPanel panel = new LazyPanel(JPanel::new);
        assertThrows(IllegalStateException.class, panel::activate);
    }
}
