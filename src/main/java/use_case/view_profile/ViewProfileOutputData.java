package use_case.view_profile;

/** Output data for the view-profile use case: the values to display. */
public class ViewProfileOutputData {
    private final String displayName;
    private final String biography;
    private final String avatarPath;

    public ViewProfileOutputData(String displayName, String biography, String avatarPath) {
        this.displayName = displayName;
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
