package data_access;

import entity.Board;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import use_case.edit_boards.EditBoardDataAccessInterface.UpdateResult;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class EditBoardStorageTest {
    @TempDir Path directory;

    @Test
    void updatesOnlyOwnedBoardAndPersistsChanges() {
        Path file = directory.resolve("boards.json");
        var dao = new JsonBoardDataAccessObject(file);
        dao.save(new Board("b1", "u1", "Old", List.of("p0")));
        dao.save(new Board("b2", "u2", "Other", List.of()));
        assertEquals(UpdateResult.UPDATED, dao.updateOwnedBoard("u1", "b1", "New", List.of("p1")));
        var reopened = new JsonBoardDataAccessObject(file);
        var board = reopened.findByOwnerAndId("u1", "b1").orElseThrow();
        assertEquals("New", board.getName());
        assertEquals(List.of("p1"), board.getPinIds());
        assertEquals("Other", reopened.findByOwnerAndId("u2", "b2").orElseThrow().getName());
        assertEquals(UpdateResult.NOT_FOUND, dao.updateOwnedBoard("u2", "b1", "Changed", List.of()));
        assertEquals("New", dao.findByOwnerAndId("u1", "b1").orElseThrow().getName());
    }

    @Test
    void rejectsDuplicateNameButAllowsExistingName() {
        var dao = new JsonBoardDataAccessObject(directory.resolve("boards.json"));
        dao.save(new Board("b1", "u1", "First", List.of()));
        dao.save(new Board("b2", "u1", "Second", List.of()));
        assertEquals(UpdateResult.DUPLICATE_NAME, dao.updateOwnedBoard("u1", "b1", "Second", List.of()));
        assertEquals(UpdateResult.UPDATED, dao.updateOwnedBoard("u1", "b1", "First", List.of("p1")));
        assertEquals("First", dao.findByOwnerAndId("u1", "b1").orElseThrow().getName());
    }
}
