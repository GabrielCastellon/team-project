package use_case.edit_boards;

import entity.Board;
import entity.Photo;
import entity.Pin;
import org.junit.jupiter.api.Test;
import use_case.open_boards.OpenBoardOutputData;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class EditBoardInteractorTest {
    private static final class Storage implements EditBoardDataAccessInterface {
        private Board board = new Board("b1", "u1", "Original", List.of("p1"));
        private int writes;
        public Optional<Board> findByOwnerAndId(String userId, String boardId) {
            return board.getUserId().equals(userId) && board.getId().equals(boardId)
                    ? Optional.of(board) : Optional.empty();
        }
        public UpdateResult updateOwnedBoard(String userId, String boardId, String name, List<String> pins) {
            if (findByOwnerAndId(userId, boardId).isEmpty()) {
                return UpdateResult.NOT_FOUND;
            }
            writes++;
            board = new Board(boardId, userId, name, pins);
            return UpdateResult.UPDATED;
        }
    }
    private static final class Output implements EditBoardOutputBoundary {
        private EditBoardEditorData editor;
        private OpenBoardOutputData saved;
        private boolean boardNotFound;
        private String error;
        public void presentEditor(EditBoardEditorData data) { editor = data; }
        public void presentSaved(OpenBoardOutputData data) { saved = data; }
        public void presentBoardNotFound() { boardNotFound = true; }
        public void presentError(String message) { error = message; }
    }
    private final Storage storage = new Storage();
    private final Output output = new Output();
    private final Photo photo = new Photo("photo", "Description", "image", "page", "Photographer", "profile", "download");
    private final EditBoardInteractor interactor = new EditBoardInteractor(storage,
            owner -> owner.equals("u1") ? List.of(new Pin("p1", owner, photo), new Pin("p2", owner, photo)) : List.of(), output);

    @Test
    void loadsOwnedPinsAndExistingSelection() {
        interactor.load("u1", "b1");
        assertEquals("Original", output.editor.name());
        assertEquals(2, output.editor.availablePins().size());
        assertEquals(List.of("p1"), output.editor.selectedPinIds());
        assertEquals(0, storage.writes);
    }

    @Test
    void savesRenameAndNewSelection() {
        interactor.save(new EditBoardInputData("u1", "b1", " Updated ", List.of("p2")));
        assertEquals("Updated", output.saved.boardName());
        assertEquals(List.of("p2"), storage.board.getPinIds());
        assertEquals(1, storage.writes);
    }

    @Test
    void rejectsUnavailablePinAndBlankName() {
        interactor.save(new EditBoardInputData("u1", "b1", "New", List.of("foreign")));
        assertNotNull(output.error);
        assertEquals(0, storage.writes);
        interactor.save(new EditBoardInputData("u1", "b1", "  ", List.of()));
        assertEquals("Enter a board name.", output.error);
        assertEquals(0, storage.writes);
    }

    @Test
    void reportsUnavailableBoardThroughItsOwnOutput() {
        interactor.load("u2", "b1");
        assertTrue(output.boardNotFound);
        assertNull(output.editor);
        assertNull(output.error);
        output.boardNotFound = false;

        interactor.save(new EditBoardInputData("u1", "missing", "Updated", List.of()));
        assertTrue(output.boardNotFound);
        assertNull(output.saved);
        assertNull(output.error);
        assertEquals(0, storage.writes);
    }
}
