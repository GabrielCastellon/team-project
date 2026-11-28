package interface_adapter.signup;

public final class SignupViewModel {
    private final SignupState state = new SignupState();
    public SignupState getState() { return state; }
}
