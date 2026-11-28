package app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import data_access.JsonProfileDataAccessObject;
import entity.InMemorySessionManager;
import entity.SessionManager;
import entity.User;
import entity.UserProfile;

import java.awt.Component;
import java.awt.Container;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import view.ProfileView;

/** Real wiring (session → JSON DAO → interactors → presenters → view) against a temp file, no Swing window. */
class ProfileModuleTest {
    // runs the "background" work immediately so the test needs no waiting
    private static final Executor SAME_THREAD = Runnable::run;

    private static final User ALICE = new User("alice-id", "alice", "hash");
    private static final User BOB = new User("bob-id", "bob", "hash");

    @TempDir
    Path directory;

    private Path file;
    private SessionManager session;

    @BeforeEach
    void setup() {
        file = directory.resolve("profiles.json");
        session = new InMemorySessionManager();
    }

    @Test
    void signedOutShowsSignInMessageAndNoProfile() throws Exception {
        save(ALICE, "Alice");

        JPanel panel = create(SAME_THREAD);

        JLabel signIn = find(panel, "signInLabel");
        assertEquals("Sign in to view your profile", signIn.getText());
        assertTrue(shown(panel, signIn));
        assertFalse(shown(panel, button(panel, "Edit profile")));
        assertFalse(containsLabel(panel, "Alice"));
    }

    @Test
    void panelShowsSignedInUsersStoredProfile() throws Exception {
        save(ALICE, "Alice");
        save(BOB, "Bob");
        session.setCurrentUser(ALICE);

        JPanel panel = create(SAME_THREAD);

        assertEquals("Alice", this.<JLabel>find(panel, "displayNameLabel").getText());
        assertTrue(shown(panel, button(panel, "Edit profile")));
    }

    @Test
    void firstTimeSignedInUserSeesDefaultProfile() throws Exception {
        session.setCurrentUser(ALICE);

        JPanel panel = create(SAME_THREAD);

        assertEquals("New user", this.<JLabel>find(panel, "displayNameLabel").getText());
    }

    @Test
    void signingInAfterOpeningTheTabShowsThatUsersProfile() throws Exception {
        save(ALICE, "Alice");
        JPanel panel = create(SAME_THREAD);

        session.setCurrentUser(ALICE);
        flush();

        assertEquals("Alice", this.<JLabel>find(panel, "displayNameLabel").getText());
        assertTrue(shown(panel, button(panel, "Edit profile")));
    }

    @Test
    void switchingAccountsShowsTheNewUsersProfileAndDiscardsTheOpenForm() throws Exception {
        save(ALICE, "Alice");
        save(BOB, "Bob");
        session.setCurrentUser(ALICE);
        JPanel panel = create(SAME_THREAD);
        SwingUtilities.invokeAndWait(() -> {
            button(panel, "Edit profile").doClick();
            this.<JTextField>find(panel, "nameField").setText("Alice's unsaved edit");
        });

        session.setCurrentUser(BOB);
        flush();

        assertEquals("Bob", this.<JLabel>find(panel, "displayNameLabel").getText());
        assertFalse(shown(panel, find(panel, "nameField")));
        // Alice's half-finished edit was never saved, to her account or Bob's
        assertEquals("Alice", load(ALICE).displayName());
        assertEquals("Bob", load(BOB).displayName());
    }

    @Test
    void loggingOutHidesTheProfile() throws Exception {
        save(ALICE, "Alice");
        session.setCurrentUser(ALICE);
        JPanel panel = create(SAME_THREAD);

        session.logout();
        flush();

        assertTrue(shown(panel, find(panel, "signInLabel")));
        assertFalse(shown(panel, button(panel, "Edit profile")));
        assertEquals("", this.<JLabel>find(panel, "displayNameLabel").getText());
    }

    @Test
    void savedEditIsWrittenForTheSignedInUserAndLoadsInNewPanel() throws Exception {
        session.setCurrentUser(ALICE);
        JPanel panel = create(SAME_THREAD);

        SwingUtilities.invokeAndWait(() -> {
            button(panel, "Edit profile").doClick();
            this.<JTextField>find(panel, "nameField").setText("Kieran");
            button(panel, "Save").doClick();
        });
        SwingUtilities.invokeAndWait(() -> { }); // let the queued render run

        assertEquals("Profile updated", this.<JLabel>find(panel, "statusLabel").getText());
        assertEquals("Kieran", load(ALICE).displayName());
        // "restart": a brand-new panel on the same file shows the saved name
        assertEquals("Kieran", this.<JLabel>find(create(SAME_THREAD), "displayNameLabel").getText());
    }

    @Test
    void savedAvatarIsShownAsAPicture() throws Exception {
        Path avatar = directory.resolve("me.png");
        ImageIO.write(new BufferedImage(300, 200, BufferedImage.TYPE_INT_RGB), "png", avatar.toFile());
        session.setCurrentUser(ALICE);
        JPanel panel = create(SAME_THREAD);

        SwingUtilities.invokeAndWait(() -> {
            button(panel, "Edit profile").doClick();
            this.<JTextField>find(panel, "avatarField").setText(avatar.toString());
            button(panel, "Save").doClick();
        });
        flush();

        JLabel picture = find(panel, "avatarLabel");
        awaitOnEdt(() -> "Profile picture".equals(description(picture)));
        SwingUtilities.invokeAndWait(() -> {
            // cropped to a round picture, with no file name shown
            assertEquals(ProfileView.AVATAR_SIZE, picture.getIcon().getIconWidth());
            assertEquals(ProfileView.AVATAR_SIZE, picture.getIcon().getIconHeight());
            assertEquals("", picture.getText());
        });
    }

    @Test
    void avatarFileThatCanNoLongerBeReadShowsAMessageInstead() throws Exception {
        new JsonProfileDataAccessObject(file).saveProfile(new UserProfile(
                ALICE.getId(), "Alice", "", directory.resolve("deleted.png").toString(), null));
        session.setCurrentUser(ALICE);
        JPanel panel = create(SAME_THREAD);

        JLabel picture = find(panel, "avatarLabel");
        JLabel caption = find(panel, "avatarCaption");
        awaitOnEdt(() -> caption.getText().equals("Picture can't be shown"));
        // the red initial stands in for the missing photo
        SwingUtilities.invokeAndWait(() -> assertEquals("No profile picture", description(picture)));
    }

    @Test
    void invalidEditShowsErrorAndSavesNothing() throws Exception {
        session.setCurrentUser(ALICE);
        JPanel panel = create(SAME_THREAD);

        SwingUtilities.invokeAndWait(() -> {
            button(panel, "Edit profile").doClick();
            this.<JTextField>find(panel, "nameField").setText("   ");
            button(panel, "Save").doClick();
        });
        SwingUtilities.invokeAndWait(() -> { });

        assertTrue(containsLabel(panel, "Display name is required."));
        assertTrue(new JsonProfileDataAccessObject(file).findProfile(ALICE.getId()).isEmpty());
    }

    @Test
    void editIsDisabledUntilProfileHasLoaded() throws Exception {
        session.setCurrentUser(ALICE);
        List<Runnable> queued = new ArrayList<>();
        JPanel panel = create(queued::add);

        SwingUtilities.invokeAndWait(() -> assertFalse(button(panel, "Edit profile").isEnabled()));

        queued.remove(0).run(); // the load finishes
        SwingUtilities.invokeAndWait(() -> { });

        SwingUtilities.invokeAndWait(() -> assertTrue(button(panel, "Edit profile").isEnabled()));
    }

    @Test
    void cancelIsDisabledWhileSaving() throws Exception {
        session.setCurrentUser(ALICE);
        List<Runnable> queued = new ArrayList<>();
        JPanel panel = create(queued::add);
        queued.remove(0).run();
        SwingUtilities.invokeAndWait(() -> { });

        SwingUtilities.invokeAndWait(() -> {
            button(panel, "Edit profile").doClick();
            this.<JTextField>find(panel, "nameField").setText("Kieran");
            button(panel, "Save").doClick();
            assertFalse(button(panel, "Cancel").isEnabled());
        });

        queued.remove(0).run(); // the save finishes
        SwingUtilities.invokeAndWait(() -> { });

        SwingUtilities.invokeAndWait(() -> assertTrue(button(panel, "Cancel").isEnabled()));
    }

    // builds the panel on the Swing thread, then flushes the render it queued
    private JPanel create(Executor executor) throws Exception {
        JPanel[] panel = new JPanel[1];
        SwingUtilities.invokeAndWait(() -> panel[0] = ProfileModule.createPanel(session, file, executor));
        SwingUtilities.invokeAndWait(() -> { });
        return panel[0];
    }

    // the picture loads on a background worker, so poll (on the EDT) until it lands, up to 5 seconds
    private static void awaitOnEdt(BooleanSupplier condition) throws Exception {
        long deadline = System.currentTimeMillis() + 5000;
        boolean[] met = new boolean[1];
        while (true) {
            SwingUtilities.invokeAndWait(() -> met[0] = condition.getAsBoolean());
            if (met[0]) {
                return;
            }
            if (System.currentTimeMillis() > deadline) {
                throw new AssertionError("condition not met within 5 seconds");
            }
            Thread.sleep(20);
        }
    }

    // a session change queues reload() on the EDT, and reload's result queues one render: flush both
    private static void flush() throws Exception {
        SwingUtilities.invokeAndWait(() -> { });
        SwingUtilities.invokeAndWait(() -> { });
    }

    private void save(User user, String displayName) throws Exception {
        new JsonProfileDataAccessObject(file).saveProfile(
                new UserProfile(user.getId(), displayName, "", "", null));
    }

    private UserProfile load(User user) throws Exception {
        return new JsonProfileDataAccessObject(file).findProfile(user.getId()).orElseThrow();
    }

    private static String description(JLabel label) {
        return label.getAccessibleContext().getAccessibleDescription();
    }

    // true if the component and every parent up to the panel are visible, i.e. its card is the one shown
    private static boolean shown(Container root, Component component) {
        for (Component c = component; c != null && c != root; c = c.getParent()) {
            if (!c.isVisible()) {
                return false;
            }
        }
        return true;
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
