package use_case.edit_profile;

/** Output data for the edit-profile use case: the values that were saved. */
public class EditProfileOutputData {
    private final String displayName;
    private final String biography;
    private final String avatarPath;

    public EditProfileOutputData(String displayName, String biography, String avatarPath) {
        this.displayName = displayName; //saved value shown back to the user BR: success view
        this.biography = biography;
        this.avatarPath = avatarPath;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBiography() {
        return biography;
    }

    public String getAvatarPath() {
        return avatarPath;
    }
}
