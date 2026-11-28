package use_case.delete_boards;

public final class DeleteBoardInteractor implements DeleteBoardInputBoundary {
    private final DeleteBoardDataAccessInterface boardDAO;
    private final DeleteBoardOutputBoundary outputBoundary;

    public DeleteBoardInteractor(DeleteBoardDataAccessInterface boardDAO,
                                 DeleteBoardOutputBoundary outputBoundary) {
        this.boardDAO = boardDAO;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(DeleteBoardInputData data) {
        if (data == null || data.userId() == null || data.userId().isBlank()
                || data.boardId() == null || data.boardId().isBlank()) {
            outputBoundary.prepareFailView("Board not found.");
            return;
        }
        boolean deleted;
        try {
            deleted = boardDAO.deleteOwnedBoard(data.userId(), data.boardId());
        } catch (RuntimeException failure) {
            outputBoundary.prepareFailView("Could not delete the board.");
            return;
        }
        if (deleted) {
            outputBoundary.prepareSuccessView(new DeleteBoardOutputData(data.boardId()));
        } else {
            outputBoundary.prepareFailView("Board not found.");
        }
    }
}
