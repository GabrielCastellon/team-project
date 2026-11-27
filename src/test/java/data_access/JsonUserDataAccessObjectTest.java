package data_access;

import entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class JsonUserDataAccessObjectTest {
    @TempDir Path tempDir;

    @Test void userSurvivesCreatingANewDaoInstance() {
        Path file = tempDir.resolve("users.json");
        JsonStorage storage = new JsonStorage();

        JsonUserDataAccessObject firstDao =
                new JsonUserDataAccessObject(storage, file);
        User user = new User("uuid-1", "matthew", "salt:hash");
        firstDao.save(user);

        JsonUserDataAccessObject secondDao =
                new JsonUserDataAccessObject(storage, file);

        assertTrue(secondDao.existsByName("MATTHEW"));
        assertEquals("uuid-1", secondDao.get("matthew").getId());
        assertEquals("salt:hash", secondDao.get("matthew").getPasswordHash());
    }
}
