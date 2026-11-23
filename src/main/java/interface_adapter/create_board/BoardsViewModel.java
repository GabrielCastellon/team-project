package interface_adapter.create_board;

import java.util.ArrayList;
import java.util.List;

public class BoardsViewModel {
    private BoardsState state = new BoardsState();
    private final List<BoardsViewModelListener> listeners = new ArrayList<>();

    public BoardsState getState() {
        return state;
    }

    public void setState(BoardsState state) {
        this.state = state;
    }

    public void addListener(BoardsViewModelListener listener) {
        listeners.add(listener);
    }

    public void notifyStateChanged() {
        for (BoardsViewModelListener listener : listeners) {
            listener.stateChanged();
        }
    }
}
