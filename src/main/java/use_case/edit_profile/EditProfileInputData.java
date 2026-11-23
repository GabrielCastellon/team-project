package use_case.edit_profile;

/** Input data for the edit-profile use case: the values the user submitted. */
public class EditProfileInputData {
    private final String userId;
    private final String displayName;
    private final String biography;
    private final String avatarPath;

    public EditProfileInputData(String userId, String displayName, String biography, String avatarPath) {
        this.userId = userId;
        this.displayName = displayName;
        this.biography = biography;
        this.avatarPath = avatarPath;
    }

    public String getUserId(){ return this.userId; }
    public String getDisplayName(){ return this.displayName; }
    public String getBiography(){ return this.biography; }
    public String getAvatarPath(){ return this.avatarPath; }
}
