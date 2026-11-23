package use_case.signup;

public final class SignupOutputData {
    private final boolean success;
    private final String username;
    private final String errorMessage;

    public SignupOutputData(boolean success, String username, String errorMessage) {
        this.success = success;
        this.username = username;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() { return success; }
    public String getUsername() { return username; }
    public String getErrorMessage() { return errorMessage; }
}
