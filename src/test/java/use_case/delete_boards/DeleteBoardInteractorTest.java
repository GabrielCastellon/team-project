package use_case.delete_boards;

import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

class DeleteBoardInteractorTest {
    private static final class Result implements DeleteBoardOutputBoundary {
        private DeleteBoardOutputData success;
        private String error;

        public void prepareSuccessView(DeleteBoardOutputData data) {
            success = data;
        }

        public void prepareFailView(String message) {
            error = message;
        }
    }

    @Test
    void deletesOnlyRequestedOwnedBoard() {
        Result result = new Result();
        var interactor = new DeleteBoardInteractor((user, board) -> {
            assertEquals("u1", user);
            assertEquals("b1", board);
            return true;
        }, result);
        interactor.execute(new DeleteBoardInputData("u1", "b1"));
        assertEquals(new DeleteBoardOutputData("b1"), result.success);
        assertNull(result.error);
    }

    @Test
    void missingOrForeignBoardReportsFailure() {
        Result result = new Result();
        new DeleteBoardInteractor((user, board) -> false, result)
                .execute(new DeleteBoardInputData("u1", "b1"));
        assertNull(result.success);
        assertEquals("Board not found.", result.error);
    }

    @Test
    void blankOwnerDoesNotCallStorage() {
        Result result = new Result();
        AtomicBoolean called = new AtomicBoolean();
        new DeleteBoardInteractor((user, board) -> {
            called.set(true);
            return true;
        }, result).execute(new DeleteBoardInputData("", "b1"));
        assertFalse(called.get());
        assertNull(result.success);
        assertEquals("Board not found.", result.error);
    }

    @Test
    void storageFailureDoesNotReportSuccess() {
        Result result = new Result();
        new DeleteBoardInteractor((user, board) -> {
            throw new IllegalStateException("Storage failure");
        }, result).execute(new DeleteBoardInputData("u1", "b1"));
        assertNull(result.success);
        assertEquals("Could not delete the board.", result.error);
    }
}
