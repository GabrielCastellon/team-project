package use_case.edit_boards;

import java.util.List;
import use_case.open_boards.OpenBoardDataAccessInterface;

public interface EditBoardDataAccessInterface extends OpenBoardDataAccessInterface {
    enum UpdateResult { UPDATED, NOT_FOUND, DUPLICATE_NAME }

    UpdateResult updateOwnedBoard(String userId, String boardId, String name, List<String> pinIds);
}
