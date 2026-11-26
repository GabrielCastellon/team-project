package interface_adapter.profile;

import use_case.edit_profile.EditProfileOutputBoundary;
import use_case.edit_profile.EditProfileOutputData;
import use_case.edit_profile.ProfileField;

/** Turns edit-profile results into a {@link ProfileState} on the view model. */
public class EditProfilePresenter implements EditProfileOutputBoundary {
    public static final String SUCCESS_MESSAGE = "Profile updated";

    private final ProfileViewModel viewModel;

    public EditProfilePresenter(ProfileViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void prepareSuccessView(EditProfileOutputData outputData) {
        viewModel.setState(new ProfileState(
                outputData.getDisplayName(), outputData.getBiography(), outputData.getAvatarPath(),
                "", "", "", SUCCESS_MESSAGE, true));
    }

    //BR5a-5c: nothing was saved, so keep the stored values and put the error next to its field
    @Override
    public void prepareValidationFailView(ProfileField field, String message) {
        ProfileState current = viewModel.getState();
        String nameError = "";
        String bioError = "";
        String avatarError = "";
        switch (field) {
            case DISPLAY_NAME -> nameError = message;
            case BIOGRAPHY -> bioError = message;
            case AVATAR -> avatarError = message;
            default -> throw new IllegalArgumentException("Unknown field: " + field);
        }
        viewModel.setState(new ProfileState(
                current.getDisplayName(), current.getBiography(), current.getAvatarPath(),
                nameError, bioError, avatarError, "", false));
    }

    //BR6a: save failed, keep stored values; the view keeps the form filled because saved is false
    @Override
    public void prepareFailView(String message) {
        ProfileState current = viewModel.getState();
        viewModel.setState(new ProfileState(
                current.getDisplayName(), current.getBiography(), current.getAvatarPath(),
                "", "", "", message, false));
    }
}
