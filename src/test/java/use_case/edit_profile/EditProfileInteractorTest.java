package use_case.edit_profile;

import static org.junit.jupiter.api.Assertions.*;

import entity.AppearanceSettings;
import entity.Theme;
import entity.UserProfile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

class EditProfileInteractorTest {

    private static final String USER_ID = "demo-user";

    private final Map<String, UserProfile> stored = new HashMap<>();

    private final Set<String> readablePaths = new HashSet<>();

    private boolean failSave;

    private boolean failLoad;

    private final ProfileDataAccessInterface dataAccess = new ProfileDataAccessInterface() {
        @Override
        public Optional<UserProfile> findProfile(String userId) throws IOException {
            if (failLoad) {
                throw new IOException("disk unreadable");
            }
            return Optional.ofNullable(stored.get(userId));
        }

        @Override
        public void saveProfile(UserProfile profile) throws IOException {
            if (failSave) {
                throw new IOException("disk full");
            }
            stored.put(profile.userId(), profile);
        }
    };

    private final AvatarFileChecker avatarFileChecker = readablePaths::contains;

    private RecordingOutputBoundary output;
    private EditProfileInteractor interactor;

    @BeforeEach
    void setup() {
        output = new RecordingOutputBoundary();
        interactor = new EditProfileInteractor(dataAccess, avatarFileChecker, output);
    }

    //fake output boundary: records the last call made, same spirit as SavePinControllerTest's fake
    private static class RecordingOutputBoundary implements EditProfileOutputBoundary {
        EditProfileOutputData success;
        ProfileField failedField;
        String failureMessage;

        @Override
        public void prepareSuccessView(EditProfileOutputData outputData) {
            success = outputData;
        }

        @Override
        public void prepareValidationFailView(ProfileField field, String message) {
            failedField = field;
        }

        @Override
        public void prepareFailView(String message) {
            failureMessage = message;
        }
    }

    private static EditProfileInputData input(String displayName, String biography, String avatarPath) {
        return new EditProfileInputData(USER_ID, displayName, biography, avatarPath);
    }

    @Test
    void validSaveUpdatesStoredProfileAndReportsSuccess() {
        interactor.execute(input("Kieran", "Hello", ""));

        assertNotNull(output.success);
        assertEquals("Kieran", output.success.getDisplayName());
        assertEquals("Hello", output.success.getBiography());
        assertEquals("", output.success.getAvatarPath());
        assertEquals("Kieran", stored.get(USER_ID).displayName());
    }

    //BR: display name is required, even if the rest of the edit is valid
    @Test
    void blankDisplayNameFailsValidationAndSavesNothing() {
        interactor.execute(input("   ", "", ""));

        assertEquals(ProfileField.DISPLAY_NAME, output.failedField);
        assertNull(output.success);
        assertFalse(stored.containsKey(USER_ID));
    }

    //BR: display name boundary pair at the 30-character limit
    @Test
    void displayNameAcceptsThirtyCharsRejectsThirtyOne() {
        interactor.execute(input("a".repeat(30), "", ""));
        assertNotNull(output.success);

        interactor.execute(input("a".repeat(31), "", ""));
        assertEquals(ProfileField.DISPLAY_NAME, output.failedField);
    }

    //BR: biography boundary pair at the 300-character limit
    @Test
    void biographyAcceptsThreeHundredCharsRejectsThreeHundredOne() {
        interactor.execute(input("Kieran", "a".repeat(300), ""));
        assertNotNull(output.success);

        interactor.execute(input("Kieran", "a".repeat(301), ""));
        assertEquals(ProfileField.BIOGRAPHY, output.failedField);
    }

    //BR: avatar extension must be png/jpg/jpeg, case-insensitively, or have no value at all
    @Test
    void avatarRejectsBadOrMissingExtensionAcceptsImageExtensionsCaseInsensitively() {
        interactor.execute(input("Kieran", "", "avatar.gif"));
        assertEquals(ProfileField.AVATAR, output.failedField);

        interactor.execute(input("Kieran", "", "avatar"));
        assertEquals(ProfileField.AVATAR, output.failedField);

        readablePaths.add("avatar.PNG");
        interactor.execute(input("Kieran", "", "avatar.PNG"));
        assertNotNull(output.success);
    }

    //BR: a well-formed avatar path still has to point at a file that actually exists
    @Test
    void avatarWithGoodExtensionButMissingFileFailsValidation() {
        interactor.execute(input("Kieran", "", "missing.png"));

        assertEquals(ProfileField.AVATAR, output.failedField);
        assertFalse(stored.containsKey(USER_ID));
    }

    //BR: a failed validation leaves any previously stored profile exactly as it was
    @Test
    void failedValidationLeavesStoredProfileUnchanged() {
        UserProfile original = new UserProfile(USER_ID, "Original", "Bio", "", null);
        stored.put(USER_ID, original);

        interactor.execute(input("", "", ""));

        assertSame(original, stored.get(USER_ID));
    }

    //BR: a first-time user with no stored profile gets a default profile to edit from, not an error
    @Test
    void firstTimeUserStartsFromDefaultProfile() {
        interactor.execute(input("New Name", "", ""));

        assertNotNull(output.success);
        assertEquals("New Name", stored.get(USER_ID).displayName());
        assertEquals(AppearanceSettings.defaults(), stored.get(USER_ID).appearance());
    }

    //BR: this use case never touches appearance, so the existing value must survive an edit
    @Test
    void appearanceIsPreservedFromExistingProfile() {
        AppearanceSettings appearance = new AppearanceSettings(Theme.DARK, "Serif", 18, true);
        stored.put(USER_ID, new UserProfile(USER_ID, "Original", "", "", appearance));

        interactor.execute(input("Updated", "", ""));

        assertEquals(appearance, stored.get(USER_ID).appearance());
    }

    //BR6a: a save failure is reported through the output boundary, not thrown, and nothing changes
    @Test
    void saveIOExceptionReportsFailureAndLeavesStoredProfileUnchanged() {
        UserProfile original = new UserProfile(USER_ID, "Original", "", "", null);
        stored.put(USER_ID, original);
        failSave = true;

        interactor.execute(input("Updated", "", ""));

        assertEquals("Couldn't save your changes, try again", output.failureMessage);
        assertNull(output.success);
        assertSame(original, stored.get(USER_ID));
    }

    //BR: a load failure is reported through the output boundary, not thrown, and nothing is saved
    @Test
    void loadIOExceptionReportsFailureAndSavesNothing() {
        failLoad = true;

        interactor.execute(input("Kieran", "", ""));

        assertEquals("Couldn't load your profile, try again", output.failureMessage);
        assertNull(output.success);
        assertFalse(stored.containsKey(USER_ID));
    }
}
