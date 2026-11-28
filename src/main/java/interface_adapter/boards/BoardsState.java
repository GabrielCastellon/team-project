package interface_adapter.boards;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class BoardsState {
    private final Map<String, String> boards = new LinkedHashMap<>();
    private final Map<String, Integer> pinCounts = new LinkedHashMap<>();
    private String errorMessage = "";

    public Map<String, String> getBoards() {
        return Collections.unmodifiableMap(boards);
    }

    public void addBoard(String boardId, String boardName) {
        addBoard(boardId, boardName, 0);
    }

    public int getPinCount(String boardId) {
        return pinCounts.getOrDefault(boardId, 0);
    }

    public void addBoard(String boardId, String boardName, int pinCount) {
        boards.put(boardId, boardName);
        pinCounts.put(boardId, pinCount);
        errorMessage = "";
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setError(String message) {
        errorMessage = message;
    }
}
