package data_access;

import static org.junit.jupiter.api.Assertions.*;

import entity.AppearanceSettings;
import entity.Theme;
import entity.UserProfile;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;
import java.util.Optional;

class JsonProfileDataAccessObjectTest {

    @TempDir Path directory;

    private Path file() {
        return directory.resolve("profiles.json");
    }

    @Test
    void missingFileReturnsEmpty() throws IOException {
        JsonProfileDataAccessObject dao = new JsonProfileDataAccessObject(file());
        assertTrue(dao.findProfile("demo-user").isEmpty());
    }

    @Test
    void savedProfileReloadsWithAllFieldsThroughANewInstance() throws IOException {
        AppearanceSettings appearance = new AppearanceSettings(Theme.DARK, "Serif", 18, true);
        UserProfile original = new UserProfile("demo-user", "Kieran", "Hello", "avatar.png", appearance);
        new JsonProfileDataAccessObject(file()).saveProfile(original);

        // a brand-new instance proves the profile actually round-trips through disk, not a cache
        Optional<UserProfile> loaded = new JsonProfileDataAccessObject(file()).findProfile("demo-user");

        assertTrue(loaded.isPresent());
        UserProfile profile = loaded.get();
        assertEquals("demo-user", profile.userId());
        assertEquals("Kieran", profile.displayName());
        assertEquals("Hello", profile.biography());
        assertEquals("avatar.png", profile.avatarPath());
        assertEquals(appearance, profile.appearance());
    }

    @Test
    void twoUsersAreStoredIndependently() throws IOException {
        JsonProfileDataAccessObject dao = new JsonProfileDataAccessObject(file());
        dao.saveProfile(new UserProfile("u1", "Alice", "", "", null));
        dao.saveProfile(new UserProfile("u2", "Bob", "", "", null));

        JsonProfileDataAccessObject reloaded = new JsonProfileDataAccessObject(file());
        assertEquals("Alice", reloaded.findProfile("u1").get().displayName());
        assertEquals("Bob", reloaded.findProfile("u2").get().displayName());
    }

    @Test
    void savingAgainOverwritesSameUserWithoutLosingOthers() throws IOException {
        JsonProfileDataAccessObject dao = new JsonProfileDataAccessObject(file());
        dao.saveProfile(new UserProfile("u1", "Alice", "", "", null));
        dao.saveProfile(new UserProfile("u2", "Bob", "", "", null));

        dao.saveProfile(new UserProfile("u1", "Alice Updated", "", "", null));

        JsonProfileDataAccessObject reloaded = new JsonProfileDataAccessObject(file());
        assertEquals("Alice Updated", reloaded.findProfile("u1").get().displayName());
        assertEquals("Bob", reloaded.findProfile("u2").get().displayName());
    }

    @Test
    void corruptedJsonThrowsIOException() throws IOException {
        Files.writeString(file(), "{ not valid json");

        JsonProfileDataAccessObject dao = new JsonProfileDataAccessObject(file());
        assertThrows(IOException.class, () -> dao.findProfile("demo-user"));
    }

    @Test
    void outOfRangeFontSizeInStoredJsonThrowsIOException() throws IOException {
        String json = "{\"demo-user\": {\"displayName\": \"Kieran\", \"biography\": \"\", "
                + "\"avatarPath\": \"\", \"theme\": \"LIGHT\", \"fontFamily\": \"SansSerif\", "
                + "\"fontSize\": 999, \"highContrast\": false}}";
        Files.writeString(file(), json);

        JsonProfileDataAccessObject dao = new JsonProfileDataAccessObject(file());
        assertThrows(IOException.class, () -> dao.findProfile("demo-user"));
    }
}
