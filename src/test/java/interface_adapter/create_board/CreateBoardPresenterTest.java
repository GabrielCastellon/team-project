package interface_adapter.create_board;

import org.junit.jupiter.api.Test;
import use_case.create_board.CreateBoardOutputData;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CreateBoardPresenterTest {
    private final BoardsViewModel viewModel = new BoardsViewModel();
    private final CreateBoardPresenter presenter = new CreateBoardPresenter(viewModel);

    @Test
    void successAddsBoardAndClearsPreviousError() {
        viewModel.getState().setError("Enter a board name.");

        presenter.prepareSuccessView(new CreateBoardOutputData("b1", "Gym"));

        assertEquals(Map.of("b1", "Gym"), viewModel.getState().getBoards());
        assertEquals("", viewModel.getState().getErrorMessage());
    }

    @Test
    void failureShowsErrorAndKeepsExistingBoards() {
        viewModel.getState().addBoard("b1", "Gym");

        presenter.prepareFailView("Could not save the board.");

        assertEquals("Could not save the board.", viewModel.getState().getErrorMessage());
        assertEquals(Map.of("b1", "Gym"), viewModel.getState().getBoards());
    }
}
