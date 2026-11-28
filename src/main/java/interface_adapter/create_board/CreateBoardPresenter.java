package interface_adapter.create_board;

import use_case.create_board.CreateBoardOutputBoundary;
import use_case.create_board.CreateBoardOutputData;

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
