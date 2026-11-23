package interface_adapter.view_boards;

import use_case.view_boards.ViewBoardsInputBoundary;
import use_case.view_boards.ViewBoardsInputData;

public class ViewBoardsController {
    private final ViewBoardsInputBoundary inputBoundary;

    public ViewBoardsController(ViewBoardsInputBoundary inputBoundary) {
        this.inputBoundary = inputBoundary;
    }

    public void execute(String userId) {
        inputBoundary.execute(new ViewBoardsInputData(userId));
    }
}
