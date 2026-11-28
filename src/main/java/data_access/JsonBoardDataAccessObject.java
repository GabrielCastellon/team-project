package data_access;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import entity.Board;
import use_case.create_board.BoardDataAccessInterface;

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

public class JsonBoardDataAccessObject implements BoardDataAccessInterface {
    private final Path filePath;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public JsonBoardDataAccessObject(Path filePath) {
        this.filePath = filePath.toAbsolutePath();
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
