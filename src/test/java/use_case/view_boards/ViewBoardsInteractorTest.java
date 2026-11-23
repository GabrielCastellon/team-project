package use_case.view_boards;

import entity.Board;
import entity.BoardFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ViewBoardsInteractorTest {
    private final FakePresenter presenter = new FakePresenter();

    @Test
    void sendsBoardIdsAndNamesToPresenter() {
        BoardFactory factory = new BoardFactory();
        Board gym = factory.create("u17", "Gym");
        Board recipes = factory.create("u17", "Recipes");

        ViewBoardsDataAccessInterface dao = userId -> {
            assertEquals("u17", userId);
            return List.of(gym, recipes);
        };

        ViewBoardsInteractor interactor = new ViewBoardsInteractor(dao, presenter);
        interactor.execute(new ViewBoardsInputData("u17"));

        assertNotNull(presenter.result);
        assertEquals(Map.of(gym.getId(), "Gym", recipes.getId(), "Recipes"),
                presenter.result.getBoards());
        assertNull(presenter.error);
    }

    @Test
    void reportsSuccessWithEmptyListWhenUserHasNoBoards() {
        ViewBoardsDataAccessInterface dao = userId -> List.of();

        ViewBoardsInteractor interactor = new ViewBoardsInteractor(dao, presenter);
        interactor.execute(new ViewBoardsInputData("u17"));

        assertNotNull(presenter.result);
        assertTrue(presenter.result.getBoards().isEmpty());
        assertNull(presenter.error);
    }

    @Test
    void reportsFailureWhenBoardsCannotBeLoaded() {
        ViewBoardsDataAccessInterface dao = userId -> {
            throw new UncheckedIOException(new IOException("Cannot read boards"));
        };

        ViewBoardsInteractor interactor = new ViewBoardsInteractor(dao, presenter);
        interactor.execute(new ViewBoardsInputData("u17"));

        assertEquals("Could not load your boards.", presenter.error);
        assertNull(presenter.result);
    }

    private static class FakePresenter implements ViewBoardsOutputBoundary {
        private ViewBoardsOutputData result;
        private String error;

        @Override
        public void prepareSuccessView(ViewBoardsOutputData outputData) {
            result = outputData;
        }

        @Override
        public void prepareFailView(String message) {
            error = message;
        }
    }
}