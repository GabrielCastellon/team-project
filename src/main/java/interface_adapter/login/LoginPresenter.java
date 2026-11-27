package interface_adapter.login;

import entity.SessionManager;
import use_case.login.LoginOutputBoundary;
import use_case.login.LoginOutputData;

/** Presenter owns session creation; the view observes destinationView = "search". */
public final class LoginPresenter implements LoginOutputBoundary {
    private final LoginViewModel viewModel;
    private final SessionManager sessionManager;

    public LoginPresenter(LoginViewModel viewModel, SessionManager sessionManager) {
        this.viewModel = viewModel;
        this.sessionManager = sessionManager;
    }

    @Override
    public void present(LoginOutputData outputData) {
        LoginState state = viewModel.getState();
        state.setSuccess(outputData.isSuccess());
        state.setErrorMessage(outputData.getErrorMessage());
        state.setUser(outputData.getUser());

        if (outputData.isSuccess()) {
            sessionManager.setCurrentUser(outputData.getUser());
            state.setDestinationView("search");
        } else {
            state.setDestinationView(null);
        }
    }
}
