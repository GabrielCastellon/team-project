package use_case.open_boards;

import entity.Board;
import entity.Photo;
import entity.Pin;
import use_case.view_pins.ViewPinsDataAccessInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class OpenBoardInteractor implements OpenBoardInputBoundary {
    private final OpenBoardDataAccessInterface boardDAO;
    private final ViewPinsDataAccessInterface pinDAO;
    private final OpenBoardOutputBoundary output;

    public OpenBoardInteractor(OpenBoardDataAccessInterface boardDAO,
                               ViewPinsDataAccessInterface pinDAO, OpenBoardOutputBoundary output) {
        this.boardDAO = boardDAO;
        this.pinDAO = pinDAO;
        this.output = output;
    }

    @Override
    public void execute(OpenBoardInputData data) {
        if (data.userId() == null || data.userId().isBlank()) {
            output.prepareBoardNotFoundView();
            return;
        }
        Optional<OpenBoardOutputData> result;
        try {
            result = boardDAO.findByOwnerAndId(data.userId(), data.boardId())
                    .map(board -> new OpenBoardOutputData(board.getId(), board.getName(), pinsFor(board)));
        } catch (RuntimeException storageFailure) {
            output.prepareFailView("Could not load this board.");
            return;
        }
        result.ifPresentOrElse(output::prepareSuccessView, output::prepareBoardNotFoundView);
    }

    private List<PinSummary> pinsFor(Board board) {
        if (board.getPinIds().isEmpty()) {
            return List.of();
        }
        Map<String, Pin> ownedPins = new HashMap<>();
        for (Pin pin : pinDAO.findByOwnerId(board.getUserId())) {
            ownedPins.put(pin.getPinId(), pin);
        }
        List<PinSummary> summaries = new ArrayList<>();
        for (String id : board.getPinIds()) {
            Pin pin = ownedPins.get(id);
            if (pin != null) {
                Photo photo = pin.getPhoto();
                summaries.add(new PinSummary(id, photo.description(), photo.imageUrl(), photo.photographer()));
            }
        }
        return summaries;
    }
}
