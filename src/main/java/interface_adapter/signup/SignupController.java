package interface_adapter.signup;

import use_case.signup.SignupInputBoundary;
import use_case.signup.SignupInputData;

public final class SignupController {
    private final SignupInputBoundary interactor;

    public SignupController(SignupInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(String username, String password, String repeatPassword) {
        interactor.execute(new SignupInputData(username, password, repeatPassword));
    }
}
