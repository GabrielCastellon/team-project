package use_case.view_profile;

import entity.UserProfile;

import java.io.IOException;

/** Loads a user's profile for display, reporting through {@link ViewProfileOutputBoundary}. */
public class ViewProfileInteractor implements ViewProfileInputBoundary {

    private final ViewProfileDataAccessInterface dataAccess;
    private final ViewProfileOutputBoundary outputBoundary;

    public ViewProfileInteractor(
            ViewProfileDataAccessInterface dataAccess,
            ViewProfileOutputBoundary outputBoundary) {
        this.dataAccess = dataAccess;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(ViewProfileInputData inputData) {
        // precondition: a signed-in user. Without one there is no profile to read, so storage is never touched
        if (inputData.getUserId() == null || inputData.getUserId().isBlank()) {
            outputBoundary.prepareSignInRequiredView();
            return;
        }

        UserProfile profile;
        try {
            // First-time user: nothing stored yet, so show the same default profile Edit Profile starts from.
            profile = dataAccess.findProfile(inputData.getUserId())
                    .orElseGet(() -> UserProfile.defaultFor(inputData.getUserId()));
        } catch (IOException exception) {
            outputBoundary.prepareFailView("Couldn't load your profile, try again");
            return;
        }

        outputBoundary.prepareSuccessView(
                new ViewProfileOutputData(profile.displayName(), profile.biography(), profile.avatarPath()));
    }
}
