package interface_adapter.edit_boards;

import interface_adapter.boards.BoardsState;
import interface_adapter.boards.BoardsViewModel;
import interface_adapter.open_boards.OpenBoardPresenter;
import use_case.edit_boards.EditBoardEditorData;
import use_case.edit_boards.EditBoardInteractor;
import use_case.edit_boards.EditBoardOutputBoundary;
import use_case.open_boards.OpenBoardOutputData;

public final class EditBoardPresenter implements EditBoardOutputBoundary {
    private final BoardsViewModel viewModel;

    public EditBoardPresenter(BoardsViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void presentEditor(EditBoardEditorData data) {
        BoardsState state = viewModel.getState();
        var choices = data.availablePins().stream().map(pin -> new EditBoardEditorState.PinChoice(
                pin.pinId(), pin.description(), pin.imageUrl(), pin.photographer())).toList();
        state.setEditor(new EditBoardEditorState(data.boardId(), data.name(), choices, data.selectedPinIds()));
        state.setError("");
        viewModel.notifyStateChanged();
    }

    @Override
    public void presentSaved(OpenBoardOutputData data) {
        BoardsState state = viewModel.getState();
        state.addBoard(data.boardId(), data.boardName(), data.pins().size());
        state.clearEditor();
        new OpenBoardPresenter(viewModel).prepareSuccessView(data);
    }

    @Override
    public void presentError(String message) {
        BoardsState state = viewModel.getState();
        if (EditBoardInteractor.BOARD_NOT_FOUND.equals(message)) {
            state.clearOpenedBoard();
            state.clearEditor();
        } else if (state.getEditor() != null) {
            var draft = state.getEditor();
            state.setEditor(new EditBoardEditorState(draft.boardId(), draft.name(),
                    draft.availablePins(), draft.selectedPinIds()));
        }
        state.setError(message);
        viewModel.notifyStateChanged();
    }
}
