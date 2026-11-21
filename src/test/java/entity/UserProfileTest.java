package entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UserProfileTest {

    @Test
    void constructorStoresAllFields() {
        AppearanceSettings appearance = new AppearanceSettings(Theme.DARK, "Serif", 18, true);
        UserProfile profile = new UserProfile("u1", "Kieran", "Hello", "avatar.png", appearance);
        assertEquals("u1", profile.userId());
        assertEquals("Kieran", profile.displayName());
        assertEquals("Hello", profile.biography());
        assertEquals("avatar.png", profile.avatarPath());
        assertEquals(appearance, profile.appearance());
    }

    //BR: displayName is trimmed, not rejected, for surrounding whitespace
    @Test
    void displayNameIsTrimmed() {
        UserProfile profile = new UserProfile("u1", "  Kieran  ", "", "", null);
        assertEquals("Kieran", profile.displayName());
    }

    //BR: userId is required
    @Test
    void rejectNullUserId() {
        assertThrows(NullPointerException.class,
                () -> new UserProfile(null, "Kieran", "", "", null));
    }

    //BR: displayName is required; blank/length rules belong to the interactor, not the entity
    @Test
    void rejectNullDisplayName() {
        assertThrows(NullPointerException.class,
                () -> new UserProfile("u1", null, "", "", null));
    }

    //BR: a missing biography defaults to empty rather than failing
    @Test
    void nullBiographyDefaultsToEmpty() {
        UserProfile profile = new UserProfile("u1", "Kieran", null, "", null);
        assertEquals("", profile.biography());
    }

    //BR: a missing avatar path defaults to empty rather than failing
    @Test
    void nullAvatarPathDefaultsToEmpty() {
        UserProfile profile = new UserProfile("u1", "Kieran", "", null, null);
        assertEquals("", profile.avatarPath());
    }

    //BR: a missing appearance defaults to AppearanceSettings.defaults()
    @Test
    void nullAppearanceDefaultsToAppearanceDefaults() {
        UserProfile profile = new UserProfile("u1", "Kieran", "", "", null);
        assertEquals(AppearanceSettings.defaults(), profile.appearance());
    }

    //BR: a brand-new user gets a default profile instead of an error
    @Test
    void defaultForGivesNewUserDefaults() {
        UserProfile profile = UserProfile.defaultFor("demo-user");
        assertEquals("demo-user", profile.userId());
        assertEquals("New user", profile.displayName());
        assertEquals("", profile.biography());
        assertEquals("", profile.avatarPath());
        assertEquals(AppearanceSettings.defaults(), profile.appearance());
    }
}
