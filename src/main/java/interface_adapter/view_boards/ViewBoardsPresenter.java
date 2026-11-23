package interface_adapter.view_boards;

import interface_adapter.create_board.BoardsState;
import interface_adapter.create_board.BoardsViewModel;
import use_case.view_boards.ViewBoardsOutputBoundary;
import use_case.view_boards.ViewBoardsOutputData;

public class ViewBoardsPresenter implements ViewBoardsOutputBoundary {
    private final BoardsViewModel viewModel;

    public ViewBoardsPresenter(BoardsViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void prepareSuccessView(ViewBoardsOutputData outputData) {
        BoardsState loadedState = new BoardsState();
        outputData.getBoards().forEach(loadedState::addBoard);

        viewModel.setState(loadedState);
        viewModel.notifyStateChanged();
    }

    @Override
    public void prepareFailView(String error) {
        viewModel.getState().setError(error);
        viewModel.notifyStateChanged();
    }
}
