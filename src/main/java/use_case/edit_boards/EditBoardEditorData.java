package use_case.edit_boards;

import java.util.List;
import use_case.open_boards.PinSummary;

public record EditBoardEditorData(String boardId, String name,
                                  List<PinSummary> availablePins, List<String> selectedPinIds) {
    public EditBoardEditorData {
        availablePins = List.copyOf(availablePins);
        selectedPinIds = List.copyOf(selectedPinIds);
    }
}
