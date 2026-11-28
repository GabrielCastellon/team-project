package interface_adapter.boards;

import java.util.ArrayList;
import java.util.List;

public class BoardsViewModel {
    private BoardsState state = new BoardsState();
    private final List<BoardsViewModelListener> listeners = new ArrayList<>();
    private long accountVersion;
    private boolean deletePending;

    public boolean isDeletePending() {
        return deletePending;
    }

    public void setDeletePending(boolean deletePending) {
        this.deletePending = deletePending;
    }

    public long getAccountVersion() {
        return accountVersion;
    }

    public void resetForAccountChange() {
        accountVersion++;
        deletePending = false;
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
