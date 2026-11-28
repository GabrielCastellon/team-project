package interface_adapter.boards;

import java.util.ArrayList;
import java.util.List;

public class BoardsViewModel {
    private BoardsState state = new BoardsState();
    private final List<BoardsViewModelListener> listeners = new ArrayList<>();
    private long accountVersion;

    public long getAccountVersion() {
        return accountVersion;
    }

    public void resetForAccountChange() {
        accountVersion++;
        setState(new BoardsState());
        notifyStateChanged();
    }

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
