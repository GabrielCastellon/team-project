package data_access;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import entity.Photo;

import use_case.discovery.PreferenceStore;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

public final class JsonPreferenceStore implements PreferenceStore {

    private final Path directory;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public JsonPreferenceStore(Path directory) {
        this.directory = directory;
    }

    @Override
    public synchronized List<Photo> load(String profile) throws IOException {
        Path path = path(profile);
        if (!Files.exists(path)) {
            return List.of();
        }
        try {
            Photo[] photos = gson.fromJson(Files.readString(path), Photo[].class);
            if (photos == null) {
                throw new JsonParseException("Expected photo array");
            }
            for (Photo p : photos) {
                if (p == null
                        || p.id() == null
                        || p.imageUrl() == null
                        || p.pageUrl() == null
                        || p.photographer() == null
                        || p.photographerUrl() == null
                        || p.downloadUrl() == null) {
                    throw new JsonParseException("Incomplete photo metadata");
                }
            }
            return List.copyOf(Arrays.asList(photos));
        } catch (JsonParseException | IllegalStateException ex) {
            throw new IOException(
                    "Preference JSON is invalid; restore or repair " + path.getFileName(), ex);
        }
    }

    @Override
    public synchronized void save(String profile, List<Photo> photos) throws IOException {
        Files.createDirectories(directory);
        Path destination = path(profile);
        Path temporary = Files.createTempFile(directory, "preferences-", ".tmp");
        try {
            Files.writeString(temporary, gson.toJson(photos));
            try {
                Files.move(
                        temporary,
                        destination,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private Path path(String profile) {
        if (profile == null || profile.isBlank()) {
            throw new IllegalArgumentException("Empty preference ID");
        }
        try {
            byte[] hash =
                    MessageDigest.getInstance("SHA-256")
                            .digest(profile.trim().getBytes(StandardCharsets.UTF_8));
            StringBuilder name = new StringBuilder();
            for (byte b : hash) {
                name.append(String.format("%02x", b & 255));
            }
            return directory.resolve(name + ".json");
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
