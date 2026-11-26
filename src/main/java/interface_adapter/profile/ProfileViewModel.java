package interface_adapter.profile;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Holds the current {@link ProfileState} and notifies listeners when it changes (observer pattern). */
public class ProfileViewModel {
    //presenters run on the background executor thread; volatile makes the new state
    //visible to the Swing thread that reads it
    private volatile ProfileState state = ProfileState.empty();
    //thread-safe list for the same reason: setState notifies from the background thread
    private final List<ProfileViewModelListener> listeners = new CopyOnWriteArrayList<>();

    public ProfileState getState() {
        return state;
    }

    public void addListener(ProfileViewModelListener listener) {
        listeners.add(listener);
    }

    /** Replaces the state and calls {@code stateChanged()} on every listener. */
    public void setState(ProfileState state) {
        this.state = state;
        for (ProfileViewModelListener listener : listeners) {
            listener.stateChanged();
        }
    }
}
