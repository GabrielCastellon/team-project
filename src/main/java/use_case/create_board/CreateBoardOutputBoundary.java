package use_case.create_board;

public interface CreateBoardOutputBoundary {
    void prepareSuccessView(CreateBoardOutputData outputData);
    void prepareFailView(String error);
}
