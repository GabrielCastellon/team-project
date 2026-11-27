package interface_adapter.login;

public final class LoginViewModel {
    private final LoginState state = new LoginState();
    public LoginState getState() { return state; }
}
