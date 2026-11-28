package interface_adapter.create_board;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class BoardsState {
    private final Map<String, String> boards = new LinkedHashMap<>();
    private String errorMessage = "";

    public Map<String, String> getBoards() {
        return Collections.unmodifiableMap(boards);
    }

    public void addBoard(String boardId, String boardName) {
        boards.put(boardId, boardName);
        errorMessage = "";
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setError(String message) {
        errorMessage = message;
    }
}
