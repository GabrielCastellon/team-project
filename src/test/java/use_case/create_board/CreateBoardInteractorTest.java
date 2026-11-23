package use_case.create_board;

import entity.Board;
import entity.BoardFactory;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.io.UncheckedIOException;

import static org.junit.jupiter.api.Assertions.*;

class CreateBoardInteractorTest {

    @Test
    void createsBoardAndReportsSuccess() {
        FakeBoardDAO dao = new FakeBoardDAO();
        FakePresenter presenter = new FakePresenter();
        CreateBoardInteractor interactor =
                new CreateBoardInteractor(dao, presenter, new BoardFactory());

        interactor.execute(new CreateBoardInputData("u17", "Gym"));

        assertNotNull(dao.savedBoard);
        assertEquals("u17", dao.savedBoard.getUserId());
        assertEquals("Gym", dao.savedBoard.getName());

        assertNotNull(presenter.result);
        assertEquals(dao.savedBoard.getId(), presenter.result.getBoardId());
        assertEquals(dao.savedBoard.getName(), presenter.result.getBoardName());
        assertNull(presenter.errorMessage);
    }

    @Test
    void rejectsBlankBoardName() {
        FakeBoardDAO dao = new FakeBoardDAO();
        FakePresenter presenter = new FakePresenter();
        CreateBoardInteractor interactor =
                new CreateBoardInteractor(dao, presenter, new BoardFactory());

        interactor.execute(new CreateBoardInputData("u17", "   "));

        assertEquals("Enter a board name.", presenter.errorMessage);
        assertNull(dao.savedBoard);
        assertNull(presenter.result);
    }

    @Test
    void rejectsDuplicateBoardNameForSameUser() {
        FakeBoardDAO dao = new FakeBoardDAO() {
            @Override
            public boolean existsByName(String userId, String name) {
                return "u17".equals(userId) && "Gym".equals(name);
            }
        };

        FakePresenter presenter = new FakePresenter();
        CreateBoardInteractor interactor =
                new CreateBoardInteractor(dao, presenter, new BoardFactory());

        interactor.execute(new CreateBoardInputData("u17", "Gym"));

        assertEquals("You already have a board with that name.", presenter.errorMessage);
        assertNull(dao.savedBoard);
        assertNull(presenter.result);
    }

    @Test
    void reportsFailureWhenBoardCannotBeSaved() {
        FakeBoardDAO dao = new FakeBoardDAO() {
            @Override
            public void save(Board board) {
                throw new UncheckedIOException(new IOException("Disk is full"));
            }
        };

        FakePresenter presenter = new FakePresenter();
        CreateBoardInteractor interactor =
                new CreateBoardInteractor(dao, presenter, new BoardFactory());

        interactor.execute(new CreateBoardInputData("u17", "Gym"));

        assertEquals("Could not save the board.", presenter.errorMessage);
        assertNull(presenter.result);
    }

    @Test
    void reportsFailureWhenBoardLookupFails() {
        FakeBoardDAO dao = new FakeBoardDAO() {
            @Override
            public boolean existsByName(String userId, String name) {
                throw new UncheckedIOException(new IOException("Cannot read boards"));
            }
        };
        FakePresenter presenter = new FakePresenter();
        CreateBoardInteractor interactor =
                new CreateBoardInteractor(dao, presenter, new BoardFactory());

        interactor.execute(new CreateBoardInputData("u17", "Gym"));

        assertEquals("Could not save the board.", presenter.errorMessage);
        assertNull(dao.savedBoard);
        assertNull(presenter.result);
    }

    private static class FakeBoardDAO implements BoardDataAccessInterface {
        private Board savedBoard;

        @Override
        public boolean existsByName(String userId, String name) {
            return false;
        }

        @Override
        public void save(Board board) {
            this.savedBoard = board;
        }
    }

    private static class FakePresenter implements CreateBoardOutputBoundary {
        private CreateBoardOutputData result;
        private String errorMessage;

        @Override
        public void prepareSuccessView(CreateBoardOutputData outputData) {
            this.result = outputData;
        }

        @Override
        public void prepareFailView(String error) {
            this.errorMessage = error;
        }
    }
}
