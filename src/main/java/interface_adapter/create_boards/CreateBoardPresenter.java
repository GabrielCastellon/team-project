package interface_adapter.create_boards;

import interface_adapter.boards.BoardsState;
import interface_adapter.boards.BoardsViewModel;
import use_case.create_boards.CreateBoardOutputBoundary;
import use_case.create_boards.CreateBoardOutputData;

public class CreateBoardPresenter implements CreateBoardOutputBoundary {
    private final BoardsViewModel viewModel;

    public CreateBoardPresenter(BoardsViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void prepareSuccessView(CreateBoardOutputData outputData) {
        BoardsState state = viewModel.getState();
        state.addBoard(outputData.getBoardId(), outputData.getBoardName());
        viewModel.notifyStateChanged();
    }

    @Override
    public void prepareFailView(String error) {
        BoardsState state = viewModel.getState();
        state.setError(error);
        viewModel.notifyStateChanged();
    }
}
