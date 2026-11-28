package interface_adapter.edit_boards;

import java.util.List;
import use_case.edit_boards.EditBoardInputBoundary;
import use_case.edit_boards.EditBoardInputData;

public final class EditBoardController {
    private final EditBoardInputBoundary inputBoundary;

    public EditBoardController(EditBoardInputBoundary inputBoundary) {
        this.inputBoundary = inputBoundary;
    }

    public void load(String userId, String boardId) {
        inputBoundary.load(userId, boardId);
    }

    public void save(String userId, String boardId, String name, List<String> pinIds) {
        inputBoundary.save(new EditBoardInputData(userId, boardId, name, pinIds));
    }
}
