package interface_adapter.login;

import entity.User;

public final class LoginState {
    private boolean success;
    private User user;
    private String errorMessage;
    private String destinationView;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getDestinationView() { return destinationView; }
    public void setDestinationView(String destinationView) { this.destinationView = destinationView; }
}
