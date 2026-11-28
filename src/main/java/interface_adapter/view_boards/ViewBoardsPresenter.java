package interface_adapter.view_boards;

import interface_adapter.boards.BoardsState;
import interface_adapter.boards.BoardsViewModel;
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
        outputData.getBoards().forEach((id, name) ->
                loadedState.addBoard(id, name, outputData.getPinCount(id)));

        var opened = viewModel.getState().getOpenedBoard();
        if (opened != null && loadedState.getBoards().containsKey(opened.id())) {
            loadedState.setOpenedBoard(opened);
            loadedState.setEditor(viewModel.getState().getEditor());
        }
        viewModel.setState(loadedState);
        viewModel.notifyStateChanged();
    }

    @Override
    public void prepareFailView(String error) {
        viewModel.getState().setError(error);
        viewModel.notifyStateChanged();
    }
}
