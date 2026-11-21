package entity;

import java.util.Objects;

/**
 * A user's profile: identity plus editable display fields and appearance.
 * Validation here is limited to non-null checks, trimming, and defaults;
 * user-facing rules such as maximum lengths and avatar file format are
 * enforced by the EditProfile use case so it can report per-field errors.
 */
public final class UserProfile {

    private static final String DEFAULT_DISPLAY_NAME = "New user";

    private final String userId;

    private final String displayName;

    private final String biography;

    private final String avatarPath;

    private final AppearanceSettings appearance;

    public UserProfile(
            String userId,
            String displayName,
            String biography,
            String avatarPath,
            AppearanceSettings appearance) {
        this.userId = Objects.requireNonNull(userId, "userId is required.");
        this.displayName = Objects.requireNonNull(displayName, "displayName is required.").trim();
        this.biography = biography == null ? "" : biography;
        this.avatarPath = avatarPath == null ? "" : avatarPath;
        this.appearance = appearance == null ? AppearanceSettings.defaults() : appearance;
    }

    /** A brand-new profile for a user with nothing stored yet. */
    public static UserProfile defaultFor(String userId) {
        return new UserProfile(userId, DEFAULT_DISPLAY_NAME, "", "", AppearanceSettings.defaults());
    }

    public String userId() {
        return userId;
    }

    public String displayName() {
        return displayName;
    }

    public String biography() {
        return biography;
    }

    public String avatarPath() {
        return avatarPath;
    }

    public AppearanceSettings appearance() {
        return appearance;
    }
}
