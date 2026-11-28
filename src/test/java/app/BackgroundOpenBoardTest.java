package app;

import entity.Board;
import interface_adapter.boards.BoardsViewModel;
import interface_adapter.open_boards.OpenBoardPresenter;
import interface_adapter.discovery.TaskRunner;
import org.junit.jupiter.api.Test;
import use_case.open_boards.OpenBoardInputData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;

class BackgroundOpenBoardTest {
    private final BoardsViewModel model = new BoardsViewModel();
    private final List<Runnable> deliveries = new ArrayList<>();
    private final TaskRunner<Runnable> delayed = this::queue;

    private void queue(Callable<Runnable> read, BiConsumer<Runnable, Throwable> deliver) {
        try {
            Runnable result = read.call();
            deliveries.add(() -> deliver.accept(result, null));
        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }

    private final BackgroundOpenBoard open = new BackgroundOpenBoard(
            (owner, id) -> Optional.of(new Board(id, owner, id, List.of())),
            owner -> { throw new AssertionError("Empty boards should skip pin storage"); },
            new OpenBoardPresenter(model), model, delayed);

    @Test
    void ignoresOlderSelectionAndResultsForPreviousAccount() {
        open.execute(new OpenBoardInputData("u1", "first"));
        open.execute(new OpenBoardInputData("u1", "second"));
        deliveries.get(1).run();
        deliveries.get(0).run();
        assertEquals("second", model.getState().getOpenedBoard().id());
        open.execute(new OpenBoardInputData("u1", "third"));
        model.resetForAccountChange();
        deliveries.get(2).run();
        assertNull(model.getState().getOpenedBoard());
    }

    @Test
    void missingBoardAndStorageFailureReturnToList() {
        var presenter = new OpenBoardPresenter(model);
        var lookup = new BackgroundOpenBoard((owner, id) -> {
            if (id.equals("missing")) return Optional.empty();
            throw new IllegalStateException("Unreadable board store");
        }, owner -> List.of(), presenter, model, delayed);
        for (String id : List.of("missing", "corrupt")) {
            open.execute(new OpenBoardInputData("u", "valid"));
            deliveries.remove(0).run();
            assertNotNull(model.getState().getOpenedBoard());
            lookup.execute(new OpenBoardInputData("u", id));
            deliveries.remove(0).run();
            assertNull(model.getState().getOpenedBoard());
            assertFalse(model.getState().getErrorMessage().isBlank());
        }
    }
}
