package use_case.edit_boards;

import java.util.List;

public record EditBoardInputData(String userId, String boardId, String name, List<String> pinIds) {
    public EditBoardInputData {
        pinIds = List.copyOf(pinIds);
    }
}
