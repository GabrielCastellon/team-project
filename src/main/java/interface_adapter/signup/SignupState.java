package interface_adapter.signup;

public final class SignupState {
    private boolean success;
    private String username = "";
    private String errorMessage;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
