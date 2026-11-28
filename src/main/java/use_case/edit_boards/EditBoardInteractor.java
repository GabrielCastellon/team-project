package use_case.edit_boards;

import entity.Board;
import entity.Pin;
import entity.Photo;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import use_case.open_boards.OpenBoardOutputData;
import use_case.open_boards.PinSummary;
import use_case.view_pins.ViewPinsDataAccessInterface;

public final class EditBoardInteractor implements EditBoardInputBoundary {
    private final EditBoardDataAccessInterface boardDAO;
    private final ViewPinsDataAccessInterface pinDAO;
    private final EditBoardOutputBoundary outputBoundary;

    public EditBoardInteractor(EditBoardDataAccessInterface boardDAO,
                               ViewPinsDataAccessInterface pinDAO, EditBoardOutputBoundary outputBoundary) {
        this.boardDAO = boardDAO;
        this.pinDAO = pinDAO;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void load(String userId, String boardId) {
        if (userId == null || userId.isBlank()) {
            outputBoundary.presentBoardNotFound();
            return;
        }
        EditBoardEditorData editorData;
        try {
            var result = boardDAO.findByOwnerAndId(userId, boardId);
            if (result.isEmpty()) {
                outputBoundary.presentBoardNotFound();
                return;
            }
            Board board = result.get();
            Map<String, PinSummary> pins = ownedPins(userId);
            editorData = new EditBoardEditorData(board.getId(), board.getName(),
                    List.copyOf(pins.values()), board.getPinIds().stream().filter(pins::containsKey).toList());
        } catch (RuntimeException failure) {
            outputBoundary.presentError("Could not load the board editor.");
            return;
        }
        outputBoundary.presentEditor(editorData);
    }

    @Override
    public void save(EditBoardInputData data) {
        if (data.userId() == null || data.userId().isBlank()) {
            outputBoundary.presentBoardNotFound();
            return;
        }
        if (data.name() == null || data.name().isBlank()) {
            outputBoundary.presentError("Enter a board name.");
            return;
        }
        String name = data.name().strip();
        List<String> selected = List.copyOf(new LinkedHashSet<>(data.pinIds()));
        OpenBoardOutputData saved;
        try {
            Map<String, PinSummary> pins = ownedPins(data.userId());
            if (!pins.keySet().containsAll(selected)) {
                outputBoundary.presentError("One or more selected pins are unavailable.");
                return;
            }
            saved = new OpenBoardOutputData(data.boardId(), name, selected.stream().map(pins::get).toList());
            var result = boardDAO.updateOwnedBoard(data.userId(), data.boardId(), name, selected);
            if (result == EditBoardDataAccessInterface.UpdateResult.NOT_FOUND) {
                outputBoundary.presentBoardNotFound();
                return;
            }
            if (result == EditBoardDataAccessInterface.UpdateResult.DUPLICATE_NAME) {
                outputBoundary.presentError("You already have a board with that name.");
                return;
            }
        } catch (RuntimeException failure) {
            outputBoundary.presentError("Could not save the board.");
            return;
        }
        outputBoundary.presentSaved(saved);
    }

    private Map<String, PinSummary> ownedPins(String userId) {
        Map<String, PinSummary> result = new LinkedHashMap<>();
        for (Pin pin : pinDAO.findByOwnerId(userId)) {
            Photo photo = pin.getPhoto();
            result.put(pin.getPinId(), new PinSummary(pin.getPinId(), photo.description(),
                    photo.imageUrl(), photo.photographer()));
        }
        return result;
    }
}
