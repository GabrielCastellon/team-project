package use_case.create_board;

import entity.Board;
import entity.BoardFactory;

public class CreateBoardInteractor implements CreateBoardInputBoundary {
    private final BoardDataAccessInterface boardDAO;
    private final CreateBoardOutputBoundary outputBoundary;
    private final BoardFactory boardFactory;

    public CreateBoardInteractor(BoardDataAccessInterface boardDAO,
                                 CreateBoardOutputBoundary outputBoundary,
                                 BoardFactory boardFactory) {
        this.boardDAO = boardDAO;
        this.outputBoundary = outputBoundary;
        this.boardFactory = boardFactory;
    }

    @Override
    public void execute(CreateBoardInputData inputData) {
        String userId = inputData.getUserId();
        String name = inputData.getName();

        if (name == null || name.isBlank()) {
            outputBoundary.prepareFailView("Enter a board name.");
            return;
        }

        name = name.strip();

        if (boardDAO.existsByName(userId, name)) {
            outputBoundary.prepareFailView("You already have a board with that name.");
            return;
        }

        Board board = boardFactory.create(userId, name);
        boardDAO.save(board);

        CreateBoardOutputData outputData =
                new CreateBoardOutputData(board.getId(), board.getName());
        outputBoundary.prepareSuccessView(outputData);
    }
}
