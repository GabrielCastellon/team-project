package interface_adapter.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import use_case.view_profile.ViewProfileOutputData;

class ViewProfilePresenterTest {

    private final ProfileViewModel viewModel = new ProfileViewModel();
    private final ViewProfilePresenter presenter = new ViewProfilePresenter(viewModel);

    @Test
    void successShowsValuesClearsErrorsAndStatusAndIsSignedIn() {
        viewModel.setState(new ProfileState("Old", "", "", "bad", "bad", "bad", "old status", false, false));

        presenter.prepareSuccessView(new ViewProfileOutputData("Kieran", "bio", "/tmp/me.png"));

        ProfileState state = viewModel.getState();
        assertEquals("Kieran", state.getDisplayName());
        assertEquals("bio", state.getBiography());
        assertEquals("/tmp/me.png", state.getAvatarPath());
        assertEquals("", state.getNameError());
        assertEquals("", state.getBioError());
        assertEquals("", state.getAvatarError());
        assertEquals("", state.getStatusMessage());
        assertFalse(state.isSaved());
        assertTrue(state.isSignedIn());
    }

    //values on screen may belong to the previous account, so a failed load must not keep them
    @Test
    void failShowsMessageAndClearsPreviousValues() {
        viewModel.setState(new ProfileState("Alice", "bio", "/tmp/a.png", "", "", "", "", false, true));

        presenter.prepareFailView("Couldn't load your profile, try again");

        ProfileState state = viewModel.getState();
        assertEquals("Couldn't load your profile, try again", state.getStatusMessage());
        assertEquals("", state.getDisplayName());
        assertEquals("", state.getBiography());
        assertEquals("", state.getAvatarPath());
        assertTrue(state.isSignedIn());
    }

    @Test
    void signInRequiredHidesProfileAndAsksToSignIn() {
        viewModel.setState(new ProfileState("Alice", "bio", "/tmp/a.png", "", "", "", "", false, true));

        presenter.prepareSignInRequiredView();

        ProfileState state = viewModel.getState();
        assertFalse(state.isSignedIn());
        assertEquals("Sign in to view your profile", state.getStatusMessage());
        assertEquals("", state.getDisplayName());
        assertEquals("", state.getBiography());
        assertEquals("", state.getAvatarPath());
    }
}
