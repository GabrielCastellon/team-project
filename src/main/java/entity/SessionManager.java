package entity;

/** Holds the user currently authenticated in this application process. */
public interface SessionManager {
    User getCurrentUser();
    void setCurrentUser(User user);
    boolean isLoggedIn();
    void logout();

    /** Runs after a sign-in, account switch or logout for this application session. */
    void addSessionListener(Runnable listener);
}
