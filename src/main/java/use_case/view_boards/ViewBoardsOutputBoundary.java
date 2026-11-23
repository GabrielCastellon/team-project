package use_case.view_boards;

public interface ViewBoardsOutputBoundary {
    void prepareSuccessView(ViewBoardsOutputData outputData);
    void prepareFailView(String error);
}