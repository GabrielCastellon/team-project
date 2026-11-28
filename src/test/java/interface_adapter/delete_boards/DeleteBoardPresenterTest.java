package interface_adapter.delete_boards;

import interface_adapter.boards.BoardsViewModel;
import interface_adapter.open_boards.OpenBoardPresenter.OpenedBoard;
import org.junit.jupiter.api.Test;
import use_case.delete_boards.DeleteBoardOutputData;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class DeleteBoardPresenterTest {
    @Test
    void removesBoardAndReturnsToList() {
        var model = new BoardsViewModel();
        var state = model.getState();
        state.addBoard("b1", "Delete", 2);
        state.addBoard("b2", "Keep", 1);
        state.setOpenedBoard(new OpenedBoard("b1", "Delete", List.of()));
        var notifications = new AtomicInteger();
        model.addListener(notifications::incrementAndGet);
        new DeleteBoardPresenter(model).prepareSuccessView(new DeleteBoardOutputData("b1"));
        assertFalse(state.getBoards().containsKey("b1"));
        assertEquals(0, state.getPinCount("b1"));
        assertEquals("Keep", state.getBoards().get("b2"));
        assertNull(state.getOpenedBoard());
        assertNull(state.getEditor());
        assertEquals(1, notifications.get());
    }

    @Test
    void failureLeavesBoardUnchanged() {
        var model = new BoardsViewModel();
        model.getState().addBoard("b1", "Keep", 1);
        new DeleteBoardPresenter(model).prepareFailView("Could not delete the board.");
        assertEquals("Keep", model.getState().getBoards().get("b1"));
        assertEquals("Could not delete the board.", model.getState().getErrorMessage());
    }
}
