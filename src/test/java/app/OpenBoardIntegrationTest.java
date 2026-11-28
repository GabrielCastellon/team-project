package app;

import data_access.JsonBoardDataAccessObject;
import data_access.JsonPinDataAccessObject;
import entity.*;
import interface_adapter.view_pins.ViewPinsPresenter.PinCard;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import view.BoardsView;

import javax.swing.*;
import java.awt.Component;
import java.awt.Container;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;

class OpenBoardIntegrationTest {
    @TempDir Path directory;

    @Test
    void opensSortedCardWithOnlyItsOwnedPinsThenReturnsToList() throws Exception {
        Path boardsFile = directory.resolve("boards.json");
        Path pinsFile = directory.resolve("pins.json");
        var boardDAO = new JsonBoardDataAccessObject(boardsFile);
        boardDAO.save(new Board("billie", "gabe", "Billie", List.of("second", "foreign", "missing", "first")));
        boardDAO.save(new Board("food", "gabe", "Food", List.of()));
        boardDAO.save(new Board("private", "other", "Private", List.of()));
        var pinDAO = new JsonPinDataAccessObject(pinsFile);
        for (String id : List.of("first", "foreign", "unrelated", "second")) {
            String owner = id.equals("foreign") ? "other" : "gabe";
            pinDAO.save(new Pin(id, owner, new Photo(id, id, id, "", "Artist", "", "")));
        }
        var session = new InMemorySessionManager();
        session.setCurrentUser(new User("gabe", "Gabriel", "test-hash"));
        BoardsView view = onEdt(() -> {
            BoardsView panel = BoardsModule.createPanel(session, boardsFile, pinsFile);
            panel.addNotify();
            return panel;
        });
        try {
            await(() -> button(view, "Open board Billie") != null);
            onEdt(() -> {
                assertNull(button(view, "Open board Private"));
                button(view, "Open board Billie").doClick();
                return null;
            });
            await(() -> button(view, "Back to boards") != null);
            onEdt(() -> {
                assertNotNull(find(view, JLabel.class, "Billie"));
                JList<?> list = (JList<?>) find(view, JList.class, null);
                assertEquals(2, list.getModel().getSize());
                assertEquals("second", ((PinCard) list.getModel().getElementAt(0)).description());
                assertEquals("first", ((PinCard) list.getModel().getElementAt(1)).description());
                button(view, "Back to boards").doClick();
                button(view, "Open board Food").doClick();
                return null;
            });
            await(() -> find(view, JLabel.class, "No saved pins yet") != null);
            onEdt(() -> {
                view.setCurrentUserId(null);
                assertNull(button(view, "Back to boards"));
                assertNotNull(find(view, JLabel.class, "Sign in to view your boards"));
                return null;
            });
        } finally {
            onEdt(() -> { view.removeNotify(); return null; });
        }
    }

    private static JButton button(Container root, String name) {
        return (JButton) find(root, JButton.class, name);
    }

    private static Component find(Container root, Class<?> type, String name) {
        for (Component child : root.getComponents()) {
            if (!child.isVisible()) continue;
            if (type.isInstance(child) && (name == null
                    || name.equals(child.getAccessibleContext().getAccessibleName()))) return child;
            if (child instanceof Container nested) {
                Component result = find(nested, type, name);
                if (result != null) return result;
            }
        }
        return null;
    }

    private static void await(BooleanSupplier condition) throws Exception {
        long end = System.nanoTime() + 5_000_000_000L;
        while (System.nanoTime() < end) {
            if (onEdt(condition::getAsBoolean)) return;
            Thread.sleep(15);
        }
        fail("Timed out waiting for board navigation");
    }

    private static <T> T onEdt(Callable<T> operation) throws Exception {
        FutureTask<T> task = new FutureTask<>(operation);
        SwingUtilities.invokeAndWait(task);
        return task.get();
    }
}
