package data_access;

import entity.Board;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DeleteBoardStorageTest {
    @TempDir Path directory;

    @Test
    void persistsDeletionAndPreservesOtherBoardsAndSavedPins() throws Exception {
        Path boardsFile = directory.resolve("boards.json");
        Path pinsFile = directory.resolve("pins.json");
        var dao = new JsonBoardDataAccessObject(boardsFile);
        dao.save(new Board("b1", "u1", "Delete", List.of("p1")));
        dao.save(new Board("b2", "u1", "Keep", List.of("p1")));
        dao.save(new Board("b3", "u2", "Private", List.of()));
        String pins = "[{\"pinId\":\"p1\",\"ownerId\":\"u1\",\"photo\":{\"id\":\"photo1\"}}]";
        Files.writeString(pinsFile, pins);

        assertTrue(dao.deleteOwnedBoard("u1", "b1"));
        var reopened = new JsonBoardDataAccessObject(boardsFile);
        assertTrue(reopened.findByOwnerAndId("u1", "b1").isEmpty());
        assertEquals(List.of("p1"), reopened.findByOwnerAndId("u1", "b2")
                .orElseThrow().getPinIds());
        assertTrue(reopened.findByOwnerAndId("u2", "b3").isPresent());
        assertEquals(pins, Files.readString(pinsFile));
    }

    @Test
    void missingOrForeignBoardLeavesFileUnchanged() throws Exception {
        Path file = directory.resolve("boards.json");
        var dao = new JsonBoardDataAccessObject(file);
        dao.save(new Board("b1", "u1", "Keep", List.of()));
        String original = Files.readString(file);
        assertFalse(dao.deleteOwnedBoard("u2", "b1"));
        assertFalse(dao.deleteOwnedBoard("u1", "missing"));
        assertEquals(original, Files.readString(file));
    }
}
