package interface_adapter.profile;

/**
 * Snapshot of everything the profile screen shows. The name, biography and avatar are
 * always the <em>stored</em> values (what display mode shows and what Cancel restores);
 * the edit form's text belongs to the view. Plain strings only, so the view never
 * needs a use-case type.
 */
public class ProfileState {
    private final String displayName;
    private final String biography;
    private final String avatarPath;
    private final String nameError;
    private final String bioError;
    private final String avatarError;
    private final String statusMessage;
    //true only right after a successful save: tells the view to leave edit mode
    private final boolean saved;

    public ProfileState(String displayName, String biography, String avatarPath,
                        String nameError, String bioError, String avatarError,
                        String statusMessage, boolean saved) {
        this.displayName = displayName;
        this.biography = biography;
        this.avatarPath = avatarPath;
        this.nameError = nameError;
        this.bioError = bioError;
        this.avatarError = avatarError;
        this.statusMessage = statusMessage;
        this.saved = saved;
    }

    /** The state before anything has loaded: every field empty. */
    public static ProfileState empty() {
        return new ProfileState("", "", "", "", "", "", "", false);
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

    public String getNameError() {
        return nameError;
    }

    public String getBioError() {
        return bioError;
    }

    public String getAvatarError() {
        return avatarError;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public boolean isSaved() {
        return saved;
    }
}
