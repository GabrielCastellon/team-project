package use_case.create_board;

import entity.Board;
import entity.BoardFactory;
import java.io.UncheckedIOException;

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

        Board board;
        try {
            if (boardDAO.existsByName(userId, name)) {
                outputBoundary.prepareFailView("You already have a board with that name.");
                return;
            }

            board = boardFactory.create(userId, name);
            boardDAO.save(board);
        } catch (UncheckedIOException e) {
            outputBoundary.prepareFailView("Could not save the board.");
            return;
        }
        CreateBoardOutputData outputData =
                new CreateBoardOutputData(board.getId(), board.getName());
        outputBoundary.prepareSuccessView(outputData);
    }
}
