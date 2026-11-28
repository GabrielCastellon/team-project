package entity;

/** Holds the user currently authenticated in this application process. */
public interface SessionManager {
    User getCurrentUser();
    void setCurrentUser(User user);
    boolean isLoggedIn();
    void logout();
}
