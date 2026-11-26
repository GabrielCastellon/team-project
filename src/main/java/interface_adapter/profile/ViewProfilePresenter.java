package interface_adapter.profile;

import use_case.view_profile.ViewProfileOutputBoundary;
import use_case.view_profile.ViewProfileOutputData;

/** Turns view-profile results into a {@link ProfileState} on the view model. */
public class ViewProfilePresenter implements ViewProfileOutputBoundary {
    private final ProfileViewModel viewModel;

    public ViewProfilePresenter(ProfileViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void prepareSuccessView(ViewProfileOutputData outputData) {
        viewModel.setState(new ProfileState(
                outputData.getDisplayName(), outputData.getBiography(), outputData.getAvatarPath(),
                "", "", "", "", false));
    }

    @Override
    public void prepareFailView(String message) {
        //keep whatever was shown before; just report the problem
        ProfileState current = viewModel.getState();
        viewModel.setState(new ProfileState(
                current.getDisplayName(), current.getBiography(), current.getAvatarPath(),
                "", "", "", message, false));
    }
}
