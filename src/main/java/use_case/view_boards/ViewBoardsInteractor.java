package use_case.view_boards;

import entity.Board;

import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ViewBoardsInteractor implements ViewBoardsInputBoundary {
    private final ViewBoardsDataAccessInterface boardDAO;
    private final ViewBoardsOutputBoundary outputBoundary;

    public ViewBoardsInteractor(ViewBoardsDataAccessInterface boardDAO,
                                ViewBoardsOutputBoundary outputBoundary) {
        this.boardDAO = boardDAO;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(ViewBoardsInputData inputData) {
        List<Board> boards;
        try {
            boards = boardDAO.findByUserId(inputData.getUserId());
        } catch (UncheckedIOException e) {
            outputBoundary.prepareFailView("Could not load your boards.");
            return;
        }

        Map<String, String> boardNamesById = new LinkedHashMap<>();
        for (Board board : boards) {
            boardNamesById.put(board.getId(), board.getName());
        }

        outputBoundary.prepareSuccessView(new ViewBoardsOutputData(boardNamesById));
    }
}