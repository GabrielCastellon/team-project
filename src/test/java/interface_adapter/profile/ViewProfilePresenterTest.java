package interface_adapter.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import use_case.view_profile.ViewProfileOutputData;

class ViewProfilePresenterTest {

    private final ProfileViewModel viewModel = new ProfileViewModel();
    private final ViewProfilePresenter presenter = new ViewProfilePresenter(viewModel);

    @Test
    void successShowsValuesAndClearsErrorsAndStatus() {
        viewModel.setState(new ProfileState("Old", "", "", "bad", "bad", "bad", "old status", false));

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
    }

    @Test
    void failShowsMessageAndKeepsShownValues() {
        viewModel.setState(new ProfileState("Kieran", "bio", "", "", "", "", "", false));

        presenter.prepareFailView("Couldn't load your profile, try again");

        ProfileState state = viewModel.getState();
        assertEquals("Couldn't load your profile, try again", state.getStatusMessage());
        assertEquals("Kieran", state.getDisplayName());
        assertFalse(state.isSaved());
    }
}
