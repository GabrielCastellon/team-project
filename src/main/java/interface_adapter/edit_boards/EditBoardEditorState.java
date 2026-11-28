package interface_adapter.edit_boards;

import java.util.List;

public record EditBoardEditorState(String boardId, String name,
                                   List<PinChoice> availablePins, List<String> selectedPinIds) {
    public record PinChoice(String pinId, String description, String imageUrl, String photographer) { }

    public EditBoardEditorState {
        availablePins = List.copyOf(availablePins);
        selectedPinIds = List.copyOf(selectedPinIds);
    }
}
