package data_access;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import entity.AppearanceSettings;
import entity.Theme;
import entity.UserProfile;

import use_case.edit_profile.ProfileDataAccessInterface;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Stores every user's profile in one JSON file, keyed by userId. */
public final class JsonProfileDataAccessObject implements ProfileDataAccessInterface {

    private final Path file;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public JsonProfileDataAccessObject(Path file) {
        this.file = file;
    }

    @Override
    public synchronized Optional<UserProfile> findProfile(String userId) throws IOException {
        ProfileRecord record = readAll().get(userId);
        if (record == null) {
            return Optional.empty();
        }
        return Optional.of(toProfile(userId, record));
    }

    @Override
    public synchronized void saveProfile(UserProfile profile) throws IOException {
        Map<String, ProfileRecord> records = readAll();
        records.put(profile.userId(), toRecord(profile));
        writeAll(records);
    }

    private Map<String, ProfileRecord> readAll() throws IOException {
        if (!Files.exists(file)) {
            return new LinkedHashMap<>();
        }
        try {
            Type type = new TypeToken<Map<String, ProfileRecord>>() { }.getType();
            Map<String, ProfileRecord> records = gson.fromJson(Files.readString(file), type);
            if (records == null) {
                throw new JsonParseException("Expected a profile map");
            }
            return records;
        } catch (JsonParseException | IllegalStateException ex) {
            throw new IOException(
                    "Profile JSON is invalid; restore or repair " + file.getFileName(), ex);
        }
    }

    // Rebuilt through the entity constructors (not left as Gson's raw fields) so every
    // invariant UserProfile/AppearanceSettings enforce is re-checked on every load.
    private UserProfile toProfile(String userId, ProfileRecord record) throws IOException {
        try {
            Theme theme = Theme.valueOf(record.theme);
            AppearanceSettings appearance = new AppearanceSettings(
                    theme, record.fontFamily, record.fontSize, record.highContrast);
            return new UserProfile(
                    userId, record.displayName, record.biography, record.avatarPath, appearance);
        } catch (RuntimeException ex) {
            throw new IOException(
                    "Stored profile for " + userId + " is invalid; restore or repair "
                            + file.getFileName(), ex);
        }
    }

    private static ProfileRecord toRecord(UserProfile profile) {
        ProfileRecord record = new ProfileRecord();
        record.displayName = profile.displayName();
        record.biography = profile.biography();
        record.avatarPath = profile.avatarPath();
        record.theme = profile.appearance().theme().name();
        record.fontFamily = profile.appearance().fontFamily();
        record.fontSize = profile.appearance().fontSize();
        record.highContrast = profile.appearance().highContrast();
        return record;
    }

    private void writeAll(Map<String, ProfileRecord> records) throws IOException {
        Path directory = file.toAbsolutePath().getParent();
        Files.createDirectories(directory);
        Path temporary = Files.createTempFile(directory, "profiles-", ".tmp");
        try {
            Files.writeString(temporary, gson.toJson(records));
            try {
                Files.move(
                        temporary,
                        file,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    // Plain on-disk shape for one profile, kept separate from the entity itself: storing
    // entity fields directly would let Gson set them without ever running entity validation.
    private static final class ProfileRecord {
        private String displayName;
        private String biography;
        private String avatarPath;
        private String theme;
        private String fontFamily;
        private int fontSize;
        private boolean highContrast;
    }
}
