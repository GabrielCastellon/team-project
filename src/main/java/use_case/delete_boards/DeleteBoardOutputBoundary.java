package use_case.delete_boards;

public interface DeleteBoardOutputBoundary {
    void prepareSuccessView(DeleteBoardOutputData data);
    void prepareFailView(String error);
}
