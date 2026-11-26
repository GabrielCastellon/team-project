package app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import data_access.JsonProfileDataAccessObject;
import entity.UserProfile;

import java.awt.Component;
import java.awt.Container;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Real wiring (JSON DAO → interactors → presenters → view) against a temp file, no Swing window. */
class ProfileModuleTest {
    // runs the "background" work immediately so the test needs no waiting
    private static final Executor SAME_THREAD = Runnable::run;

    @TempDir
    Path directory;

    @Test
    void panelShowsProfileStoredInFile() throws Exception {
        Path file = directory.resolve("profiles.json");
        new JsonProfileDataAccessObject(file).saveProfile(
                new UserProfile(ProfileModule.USER_ID, "Kieran", "bio", "", null));

        JPanel panel = createOnEdt(file);

        assertEquals("Kieran", this.<JLabel>find(panel, "displayNameLabel").getText());
    }

    @Test
    void missingFileShowsDefaultProfile() throws Exception {
        JPanel panel = createOnEdt(directory.resolve("profiles.json"));

        assertEquals("New user", this.<JLabel>find(panel, "displayNameLabel").getText());
    }

    @Test
    void savedEditIsWrittenToFileAndLoadsInNewPanel() throws Exception {
        Path file = directory.resolve("profiles.json");
        JPanel panel = createOnEdt(file);

        SwingUtilities.invokeAndWait(() -> {
            button(panel, "Edit Profile").doClick();
            this.<JTextField>find(panel, "nameField").setText("Kieran");
            button(panel, "Save").doClick();
        });
        SwingUtilities.invokeAndWait(() -> { }); // let the queued render run

        assertEquals("Profile updated", this.<JLabel>find(panel, "statusLabel").getText());
        assertEquals("Kieran", new JsonProfileDataAccessObject(file)
                .findProfile(ProfileModule.USER_ID).orElseThrow().displayName());
        // "restart": a brand-new panel on the same file shows the saved name
        assertEquals("Kieran", this.<JLabel>find(createOnEdt(file), "displayNameLabel").getText());
    }

    @Test
    void invalidEditShowsErrorAndSavesNothing() throws Exception {
        Path file = directory.resolve("profiles.json");
        JPanel panel = createOnEdt(file);

        SwingUtilities.invokeAndWait(() -> {
            button(panel, "Edit Profile").doClick();
            this.<JTextField>find(panel, "nameField").setText("   ");
            button(panel, "Save").doClick();
        });
        SwingUtilities.invokeAndWait(() -> { });

        assertTrue(containsLabel(panel, "Display name is required."));
        assertTrue(new JsonProfileDataAccessObject(file).findProfile(ProfileModule.USER_ID).isEmpty());
    }

    @Test
    void editIsDisabledUntilProfileHasLoaded() throws Exception {
        List<Runnable> queued = new ArrayList<>();
        JPanel[] panel = new JPanel[1];
        SwingUtilities.invokeAndWait(() ->
                panel[0] = ProfileModule.createPanel(directory.resolve("profiles.json"), queued::add));

        SwingUtilities.invokeAndWait(() -> assertFalse(button(panel[0], "Edit Profile").isEnabled()));

        queued.remove(0).run(); // the load finishes
        SwingUtilities.invokeAndWait(() -> { });

        SwingUtilities.invokeAndWait(() -> assertTrue(button(panel[0], "Edit Profile").isEnabled()));
    }

    @Test
    void cancelIsDisabledWhileSaving() throws Exception {
        List<Runnable> queued = new ArrayList<>();
        JPanel[] panel = new JPanel[1];
        SwingUtilities.invokeAndWait(() ->
                panel[0] = ProfileModule.createPanel(directory.resolve("profiles.json"), queued::add));
        queued.remove(0).run();
        SwingUtilities.invokeAndWait(() -> { });

        SwingUtilities.invokeAndWait(() -> {
            button(panel[0], "Edit Profile").doClick();
            this.<JTextField>find(panel[0], "nameField").setText("Kieran");
            button(panel[0], "Save").doClick();
            assertFalse(button(panel[0], "Cancel").isEnabled());
        });

        queued.remove(0).run(); // the save finishes
        SwingUtilities.invokeAndWait(() -> { });

        SwingUtilities.invokeAndWait(() -> assertTrue(button(panel[0], "Cancel").isEnabled()));
    }

    // builds the panel on the Swing thread, then flushes the render it queued
    private static JPanel createOnEdt(Path file) throws Exception {
        JPanel[] panel = new JPanel[1];
        SwingUtilities.invokeAndWait(() -> panel[0] = ProfileModule.createPanel(file, SAME_THREAD));
        SwingUtilities.invokeAndWait(() -> { });
        return panel[0];
    }

    @SuppressWarnings("unchecked")
    private <T extends Component> T find(Container container, String name) {
        for (Component component : container.getComponents()) {
            if (name.equals(component.getName())) {
                return (T) component;
            }
            if (component instanceof Container child) {
                T found = find(child, name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static AbstractButton button(Container container, String text) {
        for (Component component : container.getComponents()) {
            if (component instanceof AbstractButton button && text.equals(button.getText())) {
                return button;
            }
            if (component instanceof Container child) {
                AbstractButton found = button(child, text);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static boolean containsLabel(Container container, String text) {
        for (Component component : container.getComponents()) {
            if (component instanceof JLabel label && text.equals(label.getText())) {
                return true;
            }
            if (component instanceof Container child && containsLabel(child, text)) {
                return true;
            }
        }
        return false;
    }
}
