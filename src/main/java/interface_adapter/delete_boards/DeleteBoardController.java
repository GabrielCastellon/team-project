package interface_adapter.delete_boards;

import use_case.delete_boards.DeleteBoardInputBoundary;
import use_case.delete_boards.DeleteBoardInputData;

public final class DeleteBoardController {
    private final DeleteBoardInputBoundary inputBoundary;

    public DeleteBoardController(DeleteBoardInputBoundary inputBoundary) {
        this.inputBoundary = inputBoundary;
    }

    public void execute(String userId, String boardId) {
        inputBoundary.execute(new DeleteBoardInputData(userId, boardId));
    }
}
