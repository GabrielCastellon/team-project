package interface_adapter.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.edit_profile.EditProfileOutputData;
import use_case.edit_profile.ProfileField;

class EditProfilePresenterTest {

    private ProfileViewModel viewModel;
    private EditProfilePresenter presenter;

    @BeforeEach
    void setup() {
        viewModel = new ProfileViewModel();
        presenter = new EditProfilePresenter(viewModel);
        //stored profile already on screen, with a leftover error from an earlier attempt
        viewModel.setState(new ProfileState(
                "Stored", "stored bio", "/tmp/old.png", "old error", "", "", "", false));
    }

    @Test
    void successShowsNewValuesClearsErrorsAndSetsSaved() {
        presenter.prepareSuccessView(new EditProfileOutputData("Kieran", "new bio", "/tmp/new.png"));

        ProfileState state = viewModel.getState();
        assertEquals("Kieran", state.getDisplayName());
        assertEquals("new bio", state.getBiography());
        assertEquals("/tmp/new.png", state.getAvatarPath());
        assertEquals("", state.getNameError());
        assertEquals("", state.getBioError());
        assertEquals("", state.getAvatarError());
        assertEquals("Profile updated", state.getStatusMessage());
        assertTrue(state.isSaved());
    }

    @Test
    void displayNameErrorGoesOnlyToNameField() {
        presenter.prepareValidationFailView(ProfileField.DISPLAY_NAME, "Display name is required.");

        ProfileState state = viewModel.getState();
        assertEquals("Display name is required.", state.getNameError());
        assertEquals("", state.getBioError());
        assertEquals("", state.getAvatarError());
    }

    @Test
    void biographyErrorGoesOnlyToBioField() {
        presenter.prepareValidationFailView(ProfileField.BIOGRAPHY, "Too long.");

        ProfileState state = viewModel.getState();
        assertEquals("", state.getNameError());
        assertEquals("Too long.", state.getBioError());
        assertEquals("", state.getAvatarError());
    }

    @Test
    void avatarErrorGoesOnlyToAvatarField() {
        presenter.prepareValidationFailView(ProfileField.AVATAR, "Avatar file could not be found.");

        ProfileState state = viewModel.getState();
        assertEquals("", state.getNameError());
        assertEquals("", state.getBioError());
        assertEquals("Avatar file could not be found.", state.getAvatarError());
    }

    @Test
    void validationFailureKeepsStoredValuesAndIsNotSaved() {
        presenter.prepareValidationFailView(ProfileField.BIOGRAPHY, "Too long.");

        ProfileState state = viewModel.getState();
        assertEquals("Stored", state.getDisplayName());
        assertEquals("stored bio", state.getBiography());
        assertEquals("/tmp/old.png", state.getAvatarPath());
        assertEquals("", state.getStatusMessage());
        assertFalse(state.isSaved());
    }

    @Test
    void saveFailureShowsMessageKeepsStoredValuesAndIsNotSaved() {
        presenter.prepareFailView("Couldn't save your changes, try again");

        ProfileState state = viewModel.getState();
        assertEquals("Couldn't save your changes, try again", state.getStatusMessage());
        assertEquals("Stored", state.getDisplayName());
        assertEquals("", state.getNameError());
        assertFalse(state.isSaved());
    }
}
