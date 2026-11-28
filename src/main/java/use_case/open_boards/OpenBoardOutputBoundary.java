package use_case.open_boards;

public interface OpenBoardOutputBoundary {
    void prepareSuccessView(OpenBoardOutputData board);
    void prepareBoardNotFoundView();
    void prepareFailView(String message);
}
