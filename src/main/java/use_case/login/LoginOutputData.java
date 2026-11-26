package use_case.login;

import entity.User;

public final class LoginOutputData {
    private final boolean success;
    private final User user;
    private final String errorMessage;

    public LoginOutputData(boolean success, User user, String errorMessage) {
        this.success = success;
        this.user = user;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() { return success; }
    public User getUser() { return user; }
    public String getErrorMessage() { return errorMessage; }
}
