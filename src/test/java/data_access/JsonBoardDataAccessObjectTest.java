package data_access;

import entity.BoardFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonBoardDataAccessObjectTest {
    @Test
    void keepsBothBoardsAfterReopening(@TempDir Path directory) {
        Path file = directory.resolve("boards.json");
        JsonBoardDataAccessObject dao = new JsonBoardDataAccessObject(file);
        BoardFactory factory = new BoardFactory();

        assertFalse(dao.existsByName("u17", "Gym"));
        dao.save(factory.create("u17", "Gym"));
        dao.save(factory.create("u17", "Recipes"));

        JsonBoardDataAccessObject reopened = new JsonBoardDataAccessObject(file);
        assertTrue(reopened.existsByName("u17", "Gym"));
        assertTrue(reopened.existsByName("u17", "Recipes"));
        assertFalse(reopened.existsByName("u42", "Gym"));
    }
}
