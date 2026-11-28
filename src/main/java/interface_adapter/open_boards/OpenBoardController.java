package interface_adapter.open_boards;

import use_case.open_boards.OpenBoardInputBoundary;
import use_case.open_boards.OpenBoardInputData;

public final class OpenBoardController {
    private final OpenBoardInputBoundary inputBoundary;

    public OpenBoardController(OpenBoardInputBoundary inputBoundary) {
        this.inputBoundary = inputBoundary;
    }

    public void execute(String userId, String boardId) {
        inputBoundary.execute(new OpenBoardInputData(userId, boardId));
    }
}
