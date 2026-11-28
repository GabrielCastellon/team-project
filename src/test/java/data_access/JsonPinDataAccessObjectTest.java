package data_access;

import entity.Photo;
import entity.Pin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;

public class JsonPinDataAccessObjectTest {
    @TempDir
    Path directory;

    //owner saves photo and revisits the pin with same file path under a new object
    @Test
    void testSavedPinRevisit() {
        Path file = directory.resolve("pins.json"); //provides the file path
        Photo photo = new Photo("121", "Orange Cat", "image-url",
                "page-url", "photographer",
                "profile-url", "download-url");
        Pin pin = new Pin("321", "442", photo);

        JsonPinDataAccessObject storage = new JsonPinDataAccessObject(file);
        storage.save(pin);

        JsonPinDataAccessObject revisitPin = new JsonPinDataAccessObject(file);

        assertTrue(revisitPin.existsByOwnerAndPhotoId("442", "121"));
        assertFalse(revisitPin.existsByOwnerAndPhotoId("312", "121"));

    }

    //test if right pin is matched with the owner when signed in
    @Test
    void testOwnerReceivesOwnPin() {
        Path file = directory.resolve("pins.json"); //provides the file path
        Photo photo = new Photo("121", "Orange Cat", "image-url",
                "page-url", "photographer",
                "profile-url", "download-url");
        Pin pin1 = new Pin("pin-a", "alex", photo);
        Pin pin2 = new Pin("pin-b", "bob", photo);
        JsonPinDataAccessObject storage = new JsonPinDataAccessObject(file);
        storage.save(pin1);
        storage.save(pin2);
        var result = storage.findByOwnerId("alex");
        assertEquals(1, result.size()); //catch if two user pins are returned
        //catch which pin is returned
        assertEquals("pin-a", result.get(0).getPinId());
    }

    // test if owner Id exists
    @Test
    void testLookUpOwnerId() throws IOException {
        Path file = directory.resolve("pins.json"); //directory is existing @TempDir
        Files.writeString(file, "[{}]");
        JsonPinDataAccessObject storage = new JsonPinDataAccessObject(file);

        //defines storage error caller expects
        assertThrows(IllegalStateException.class, () -> storage.existsByOwnerAndPhotoId(
                "alex", "121"));
        //checks lookup preserve file content
        assertEquals("[{}]", Files.readString(file));
    }
}
