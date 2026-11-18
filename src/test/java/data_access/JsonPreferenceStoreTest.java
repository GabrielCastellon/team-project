package data_access;

import static org.junit.jupiter.api.Assertions.*;

import entity.Photo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

class JsonPreferenceStoreTest {

    @TempDir Path directory;

    @Test
    void survivesRestartAndKeepsProfilePathsContained() throws Exception {
        JsonPreferenceStore store = new JsonPreferenceStore(directory);
        Photo p = new Photo("id", "卧室", "image", "page", "artist", "profile", "download");
        store.save("../../other", List.of(p));
        JsonPreferenceStore restarted = new JsonPreferenceStore(directory);
        assertEquals("卧室", restarted.load("../../other").get(0).description());
        assertTrue(restarted.load("different").isEmpty());
        restarted.save("../../other", List.of());
        assertTrue(restarted.load("../../other").isEmpty());
        try (var files = Files.list(directory)) {
            assertEquals(1, files.count());
        }
    }

    @Test
    void corruptFileIsNotSilentlyDiscarded() throws Exception {
        JsonPreferenceStore store = new JsonPreferenceStore(directory);
        store.save("A", List.of());
        Path file;
        try (var files = Files.list(directory)) {
            file = files.findFirst().orElseThrow();
        }
        Files.writeString(file, "{invalid");
        assertThrows(IOException.class, () -> store.load("A"));
        assertEquals("{invalid", Files.readString(file));
        Files.writeString(file, "[{}]");
        assertThrows(IOException.class, () -> store.load("A"));
    }
}
