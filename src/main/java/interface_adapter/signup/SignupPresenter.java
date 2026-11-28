package interface_adapter.signup;

import use_case.signup.SignupOutputBoundary;
import use_case.signup.SignupOutputData;

public final class SignupPresenter implements SignupOutputBoundary {
    private final SignupViewModel viewModel;

    public SignupPresenter(SignupViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void present(SignupOutputData outputData) {
        SignupState state = viewModel.getState();
        state.setSuccess(outputData.isSuccess());
        state.setUsername(outputData.getUsername());
        state.setErrorMessage(outputData.getErrorMessage());
    }
}
