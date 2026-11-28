package entity;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/** Simple in-memory session implementation; the session is not persisted. */
public final class InMemorySessionManager implements SessionManager {
    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();
    private User currentUser;

    @Override public synchronized User getCurrentUser() { return currentUser; }

    @Override public void setCurrentUser(User user) {
        if (user == null) throw new IllegalArgumentException("user cannot be null");
        changeUser(user);
    }

    @Override public synchronized boolean isLoggedIn() { return currentUser != null; }

    @Override public void logout() { changeUser(null); }

    @Override public void addSessionListener(Runnable listener) {
        listeners.add(Objects.requireNonNull(listener));
    }

    private void changeUser(User nextUser) {
        synchronized (this) {
            if (currentUser == nextUser) {
                return;
            }
            currentUser = nextUser;
        }
        for (Runnable listener : listeners) {
            listener.run();
        }
    }
}
