package use_case.edit_profile;

import entity.UserProfile;

import java.io.IOException;
import java.util.Locale;

/** Validates and saves profile edits, reporting through {@link EditProfileOutputBoundary}. */
public class EditProfileInteractor implements EditProfileInputBoundary {

    public static final int MAX_DISPLAY_NAME_LENGTH = 30;

    public static final int MAX_BIOGRAPHY_LENGTH = 300;

    private final ProfileDataAccessInterface dataAccess;
    private final AvatarFileChecker avatarFileChecker;
    private final EditProfileOutputBoundary outputBoundary;

    public EditProfileInteractor(
            ProfileDataAccessInterface dataAccess,
            AvatarFileChecker avatarFileChecker,
            EditProfileOutputBoundary outputBoundary) {
        this.dataAccess = dataAccess;
        this.avatarFileChecker = avatarFileChecker;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(EditProfileInputData inputData) {
        // First-time user: nothing stored yet, so start from a default profile (spec's first-time flow).
        UserProfile existing;
        try {
            existing = dataAccess.findProfile(inputData.getUserId())
                    .orElseGet(() -> UserProfile.defaultFor(inputData.getUserId()));
        } catch (IOException exception) {
            outputBoundary.prepareFailView("Couldn't load your profile, try again");
            return;
        }

        String displayName = inputData.getDisplayName() == null ? "" : inputData.getDisplayName().trim();
        if (displayName.isEmpty()) {
            outputBoundary.prepareValidationFailView(ProfileField.DISPLAY_NAME, "Display name is required.");
            return;
        }
        if (displayName.length() > MAX_DISPLAY_NAME_LENGTH) {
            outputBoundary.prepareValidationFailView(ProfileField.DISPLAY_NAME,
                    "Display name must be " + MAX_DISPLAY_NAME_LENGTH + " characters or fewer.");
            return;
        }

        String biography = inputData.getBiography() == null ? "" : inputData.getBiography();
        if (biography.length() > MAX_BIOGRAPHY_LENGTH) {
            outputBoundary.prepareValidationFailView(ProfileField.BIOGRAPHY,
                    "Biography must be " + MAX_BIOGRAPHY_LENGTH + " characters or fewer.");
            return;
        }

        String avatarPath = inputData.getAvatarPath() == null ? "" : inputData.getAvatarPath();
        if (!avatarPath.isEmpty()) {
            if (!hasImageExtension(avatarPath)) {
                outputBoundary.prepareValidationFailView(ProfileField.AVATAR,
                        "Avatar must be a .png, .jpg, or .jpeg file.");
                return;
            }
            if (!avatarFileChecker.isReadableFile(avatarPath)) {
                outputBoundary.prepareValidationFailView(ProfileField.AVATAR,
                        "Avatar file could not be found.");
                return;
            }
        }

        // Appearance isn't part of this use case's input; carry the existing value forward unchanged.
        UserProfile updated = new UserProfile(
                inputData.getUserId(), displayName, biography, avatarPath, existing.appearance());

        try {
            dataAccess.saveProfile(updated);
        } catch (IOException exception) {
            outputBoundary.prepareFailView("Couldn't save your changes, try again");
            return;
        }

        outputBoundary.prepareSuccessView(
                new EditProfileOutputData(updated.displayName(), updated.biography(), updated.avatarPath()));
    }

    private static boolean hasImageExtension(String path) {
        String lowerPath = path.toLowerCase(Locale.ROOT);
        return lowerPath.endsWith(".png") || lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg");
    }
}
