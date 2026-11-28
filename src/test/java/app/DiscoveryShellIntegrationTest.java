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

import interface_adapter.view_pins.ViewPinsPresenter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import view.DiscoveryView;
import view.MainView;
import view.PlaceholderView;
import app.presentation.AuthPanel;
import view.BoardsView;
import static org.junit.jupiter.api.Assertions.*;
import view.PinsView;
import data_access.JsonPinDataAccessObject;
import entity.Photo;
import entity.Pin;
import entity.InMemorySessionManager;
import entity.User;
import javax.swing.JList;
import javax.swing.JScrollPane;

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
            assertInstanceOf(AuthPanel.class, content(tabs));
            tabs.setSelectedIndex(tabs.indexOfTab("Discovery"));
            JPanel discovery = content(tabs);
            assertInstanceOf(DiscoveryView.class, discovery);
            assertEquals(1, initializations.get());
            tabs.setSelectedIndex(tabs.indexOfTab("Boards"));
            assertInstanceOf(BoardsView.class, content(tabs));
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
            assertInstanceOf(AuthPanel.class, content(tabs));
            tabs.setSelectedIndex(tabs.indexOfTab("Pins"));
            assertInstanceOf(PinsView.class, content(tabs));
        });
    }

    //tests if the saved pins associated with a user is not viewable when switching accounts
    @Test
    void testSwitchAccountPins() throws Exception{
        Path pinsFile = directory.resolve("pins.json");
        JsonPinDataAccessObject storage = new JsonPinDataAccessObject(pinsFile);

        Photo cat = new Photo("cat", "Gipsy the Cat",
                "cat.jpg", "cat-page",
                "Manja", "manja-page", "cat-download");
        Photo bedroom = new Photo("bedroom", "Bedroom",
                "bedroom.jpg", "bedroom-page",
                "Spacejoy", "spacejoy-page",
                "bedroom-download");

        storage.save(new Pin("pin-1", "alex", cat));
        storage.save(new Pin("pin-2", "gabe", bedroom));

        InMemorySessionManager session = new InMemorySessionManager();
        User user1 = new User("alex", "Alex", "test-hash");
        session.setCurrentUser(user1); //sets current user to user1

        SwingUtilities.invokeAndWait(() -> {
            MainView shell = new ApplicationFactory(session, pinsFile).createView();
            /*check user1 sees exactly one pin "Gipsy the cat" when they open the pin tab. test
        nagivates through the hierarchies of view until it reaches list of saved photos
         */
            shell.addNotify(); //mainview shell notifies swing that component is being connected
            try{
                shell.selectTab("Pins"); //goes to pins tab
                JTabbedPane tabs =
                        assertInstanceOf(JTabbedPane.class, shell.getComponent(0));

                JPanel pinsPanel = content(tabs); //actual contents of the pins tab inside LazyPanel

                //allows scrolling through the saved pins
                JScrollPane scroll =
                        assertInstanceOf(JScrollPane.class, pinsPanel.getComponent(0));

                //JList pins stores displayed descriptions
                JList<?> pins =
                        assertInstanceOf(JList.class, scroll.getViewport().getView());

                assertEquals(1, pins.getModel().getSize());

                ViewPinsPresenter.PinCard card1 = assertInstanceOf(ViewPinsPresenter.
                                PinCard.class, pins.getModel().getElementAt(0));
                assertEquals("Gipsy the Cat", card1.description());

                shell.selectTab("Accounts"); //goes to accounts
                session.logout();
                session.setCurrentUser(new User("gabe", "Gabe", "test-hash"));
                shell.selectTab("Pins");
                //switches account and checks for expected pins of second user
                assertEquals(1, pins.getModel().getSize());
                ViewPinsPresenter.PinCard card2 = assertInstanceOf(ViewPinsPresenter.PinCard.class,
                        pins.getModel().getElementAt(0));
                assertEquals("Bedroom", card2.description());
            } finally {
                shell.removeNotify();
            }
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
