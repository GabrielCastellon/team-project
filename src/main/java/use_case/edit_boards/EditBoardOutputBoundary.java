package use_case.edit_boards;

import use_case.open_boards.OpenBoardOutputData;

public interface EditBoardOutputBoundary {
    void presentEditor(EditBoardEditorData data);
    void presentSaved(OpenBoardOutputData data);
    void presentError(String message);
}
