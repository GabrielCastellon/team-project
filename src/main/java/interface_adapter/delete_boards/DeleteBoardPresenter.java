package interface_adapter.delete_boards;

import interface_adapter.boards.BoardsState;
import interface_adapter.boards.BoardsViewModel;
import use_case.delete_boards.DeleteBoardOutputBoundary;
import use_case.delete_boards.DeleteBoardOutputData;

public final class DeleteBoardPresenter implements DeleteBoardOutputBoundary {
    private final BoardsViewModel viewModel;

    public DeleteBoardPresenter(BoardsViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void prepareSuccessView(DeleteBoardOutputData data) {
        BoardsState state = viewModel.getState();
        state.removeBoard(data.boardId());
        state.setError("");
        viewModel.notifyStateChanged();
    }

    @Override
    public void prepareFailView(String error) {
        viewModel.getState().setError(error);
        viewModel.notifyStateChanged();
    }
}
