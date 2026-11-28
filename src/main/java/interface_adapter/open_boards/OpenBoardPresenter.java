package interface_adapter.open_boards;

import interface_adapter.boards.BoardsViewModel;
import interface_adapter.view_pins.ViewPinsPresenter.PinCard;
import use_case.open_boards.OpenBoardOutputBoundary;
import use_case.open_boards.OpenBoardOutputData;

import java.util.List;

public final class OpenBoardPresenter implements OpenBoardOutputBoundary {
    public record OpenedBoard(String id, String name, List<PinCard> pins) {
        public OpenedBoard {
            pins = List.copyOf(pins);
        }
    }

    private final BoardsViewModel model;

    public OpenBoardPresenter(BoardsViewModel model) {
        this.model = model;
    }

    @Override
    public void prepareSuccessView(OpenBoardOutputData board) {
        var cards = board.pins().stream()
                .map(pin -> new PinCard(pin.description(), pin.imageUrl(), pin.photographer()))
                .toList();
        model.getState().setOpenedBoard(new OpenedBoard(board.boardId(), board.boardName(), cards));
        model.getState().setError("");
        model.notifyStateChanged();
    }

    @Override
    public void prepareBoardNotFoundView() {
        prepareFailView("This board is no longer available.");
    }

    @Override
    public void prepareFailView(String message) {
        model.getState().clearOpenedBoard();
        model.getState().setError(message);
        model.notifyStateChanged();
    }
}
