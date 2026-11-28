package use_case.open_boards;

import java.util.List;

public record OpenBoardOutputData(String boardId, String boardName, List<PinSummary> pins) {
    public OpenBoardOutputData {
        pins = List.copyOf(pins);
    }
}
