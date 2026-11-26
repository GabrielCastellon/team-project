package use_case.create_boards;

public interface CreateBoardOutputBoundary {
    void prepareSuccessView(CreateBoardOutputData outputData);
    void prepareFailView(String error);
}
