package data_access;

import entity.Photo;
import entity.Pin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
