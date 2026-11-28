package interface_adapter.profile;

import use_case.view_profile.ViewProfileOutputBoundary;
import use_case.view_profile.ViewProfileOutputData;

/** Turns view-profile results into a {@link ProfileState} on the view model. */
public class ViewProfilePresenter implements ViewProfileOutputBoundary {
    public static final String SIGN_IN_MESSAGE = "Sign in to view your profile";

    private final ProfileViewModel viewModel;

    public ViewProfilePresenter(ProfileViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void prepareSuccessView(ViewProfileOutputData outputData) {
        viewModel.setState(new ProfileState(
                outputData.getDisplayName(), outputData.getBiography(), outputData.getAvatarPath(),
                "", "", "", "", false, true));
    }

    //values on screen may belong to the previously signed-in account, so they're cleared, not kept
    @Override
    public void prepareFailView(String message) {
        viewModel.setState(new ProfileState("", "", "", "", "", "", message, false, true));
    }

    @Override
    public void prepareSignInRequiredView() {
        viewModel.setState(new ProfileState("", "", "", "", "", "", SIGN_IN_MESSAGE, false, false));
    }
}
