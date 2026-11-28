package data_access;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import entity.Board;
import use_case.edit_boards.EditBoardDataAccessInterface;
import use_case.create_boards.BoardDataAccessInterface;
import use_case.view_boards.ViewBoardsDataAccessInterface;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import use_case.open_boards.OpenBoardDataAccessInterface;

public class JsonBoardDataAccessObject implements BoardDataAccessInterface, ViewBoardsDataAccessInterface, OpenBoardDataAccessInterface, EditBoardDataAccessInterface {
    private final Path filePath;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public JsonBoardDataAccessObject(Path filePath) {
        this.filePath = filePath.toAbsolutePath();
    }

    @Override
    public List<Board> findByUserId(String userId) {
        List<Board> userBoards = new ArrayList<>();

        for (Board board : readBoards()) {
            if (board.getUserId().equals(userId)) {
                userBoards.add(board);
            }
        }

        return userBoards;
    }

    @Override
    public Optional<Board> findByOwnerAndId(String userId, String boardId) {
        return findByUserId(userId).stream()
                .filter(board -> board.getId().equals(boardId))
                .findFirst();
    }

    @Override
    public boolean existsByName(String userId, String name) {
        for (Board board : readBoards()) {
            if (board.getUserId().equals(userId) && board.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized void save(Board board) {
        List<Board> boards = readBoards();
        boards.add(board);
        writeBoards(boards);
    }

    @Override
    public synchronized UpdateResult updateOwnedBoard(String userId, String boardId,
                                                       String name, List<String> pinIds) {
        List<Board> boards = readBoards();
        int target = -1;
        for (int i = 0; i < boards.size(); i++) {
            Board board = boards.get(i);
            if (board.getUserId().equals(userId) && board.getId().equals(boardId)) {
                target = i;
                break;
            }
        }
        if (target == -1) {
            return UpdateResult.NOT_FOUND;
        }
        for (Board board : boards) {
            if (board.getUserId().equals(userId) && !board.getId().equals(boardId)
                    && board.getName().equals(name)) {
                return UpdateResult.DUPLICATE_NAME;
            }
        }
        boards.set(target, new Board(boardId, userId, name, pinIds));
        writeBoards(boards);
        return UpdateResult.UPDATED;
    }

    private void writeBoards(List<Board> boards) {
        try {
            Path directory = filePath.getParent();
            Files.createDirectories(directory);
            Path tempFile = Files.createTempFile(directory, "boards-", ".tmp");

            try {
                Files.writeString(tempFile, gson.toJson(boards));
                try {
                    Files.move(tempFile, filePath,
                            StandardCopyOption.ATOMIC_MOVE,
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (AtomicMoveNotSupportedException e) {
                    Files.move(tempFile, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
            } finally {
                Files.deleteIfExists(tempFile);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save the board.", e);
        }
    }

    private List<Board> readBoards() {
        try {
            String json = Files.readString(filePath);
            Board[] boards = gson.fromJson(json, Board[].class);
            if (boards == null) {
                throw new IllegalStateException("Expected a JSON array in " + filePath);
            }
            return new ArrayList<>(Arrays.asList(boards));
        } catch (NoSuchFileException e) {
            return new ArrayList<>();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read boards.", e);
        }
    }
}
