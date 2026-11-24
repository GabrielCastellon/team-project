package interface_adapter.view_boards;

import interface_adapter.boards.BoardsViewModel;
import org.junit.jupiter.api.Test;
import use_case.view_boards.ViewBoardsOutputData;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ViewBoardsPresenterTest {
    private final BoardsViewModel viewModel = new BoardsViewModel();
    private final ViewBoardsPresenter presenter = new ViewBoardsPresenter(viewModel);

    @Test
    void successfulLoadReplacesOldBoardsAndClearsError() {
        viewModel.getState().addBoard("b1", "Old board");
        viewModel.getState().setError("Previous error");

        presenter.prepareSuccessView(new ViewBoardsOutputData(Map.of("b2", "Gym")));

        assertEquals(Map.of("b2", "Gym"), viewModel.getState().getBoards());
        assertTrue(viewModel.getState().getErrorMessage().isEmpty());
    }

    @Test
    void emptyLoadClearsPreviouslyDisplayedBoards() {
        viewModel.getState().addBoard("b1", "Old board");

        presenter.prepareSuccessView(new ViewBoardsOutputData(Map.of()));

        assertTrue(viewModel.getState().getBoards().isEmpty());
        assertTrue(viewModel.getState().getErrorMessage().isEmpty());
    }

    @Test
    void failedLoadShowsErrorAndPreservesCurrentBoards() {
        viewModel.getState().addBoard("b1", "Recipes");

        presenter.prepareFailView("Could not load your boards.");

        assertEquals("Could not load your boards.", viewModel.getState().getErrorMessage());
        assertEquals(Map.of("b1", "Recipes"), viewModel.getState().getBoards());
    }
}
