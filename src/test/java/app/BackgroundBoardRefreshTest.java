package app;

import entity.Board;
import interface_adapter.boards.BoardsViewModel;
import interface_adapter.create_board.CreateBoardPresenter;
import interface_adapter.discovery.TaskRunner;
import org.junit.jupiter.api.Test;
import use_case.create_board.CreateBoardOutputData;
import use_case.view_boards.ViewBoardsInputData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BackgroundBoardRefreshTest {
    private final BoardsViewModel model = new BoardsViewModel();
    private final List<Board> stored = new ArrayList<>();
    private final DeferredResults runner = new DeferredResults();
    private final BackgroundBoardRefresh refresh = new BackgroundBoardRefresh(
            owner -> stored.stream().filter(board -> owner.equals(board.getUserId())).toList(),
            model, runner);

    @Test
    void reloadsBothExistingAndNewBoardsWhenAnOlderReadFinishesAfterASave() {
        stored.add(new Board("old", "alice", "Recipes", List.of()));
        refresh.execute(new ViewBoardsInputData("alice"));
        stored.add(new Board("new", "alice", "Trips", List.of()));
        refresh.boardSaved();
        new CreateBoardPresenter(model).prepareSuccessView(new CreateBoardOutputData("new", "Trips"));

        runner.finish(0);
        assertEquals(Map.of("new", "Trips"), model.getState().getBoards());
        runner.finish(1);
        assertEquals(Map.of("old", "Recipes", "new", "Trips"), model.getState().getBoards());
    }

    @Test
    void ignoresAnEarlierRefreshThatFinishesLast() {
        refresh.execute(new ViewBoardsInputData("alice"));
        stored.add(new Board("new", "alice", "Trips", List.of()));
        refresh.execute(new ViewBoardsInputData("alice"));
        runner.finish(1);
        runner.finish(0);
        assertEquals(Map.of("new", "Trips"), model.getState().getBoards());
    }

    @Test
    void ignoresResultsFromAPreviousAccount() {
        stored.add(new Board("old", "alice", "Recipes", List.of()));
        refresh.execute(new ViewBoardsInputData("alice"));
        model.resetForAccountChange();
        runner.finish(0);
        assertEquals(Map.of(), model.getState().getBoards());
        assertEquals(1, runner.completions.size());
    }

    private static final class DeferredResults implements TaskRunner<Runnable> {
        private final List<Runnable> completions = new ArrayList<>();

        @Override
        public void run(Callable<Runnable> task, BiConsumer<Runnable, Throwable> onComplete) {
            try {
                Runnable result = task.call();
                completions.add(() -> onComplete.accept(result, null));
            } catch (Exception ex) {
                throw new AssertionError(ex);
            }
        }

        void finish(int index) {
            completions.get(index).run();
        }
    }
}
