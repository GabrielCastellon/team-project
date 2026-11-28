package interface_adapter.boards;

import interface_adapter.open_boards.OpenBoardPresenter.OpenedBoard;
import interface_adapter.edit_boards.EditBoardEditorState;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class BoardsState {
    private final Map<String, String> boards = new LinkedHashMap<>();
    private final Map<String, Integer> pinCounts = new LinkedHashMap<>();
    private OpenedBoard openedBoard;
    private EditBoardEditorState editor;

    public EditBoardEditorState getEditor() {
        return editor;
    }

    public void setEditor(EditBoardEditorState editor) {
        this.editor = editor;
    }

    public void clearEditor() {
        editor = null;
    }

    public OpenedBoard getOpenedBoard() {
        return openedBoard;
    }

    public void setOpenedBoard(OpenedBoard board) {
        openedBoard = board;
    }

    public void clearOpenedBoard() {
        openedBoard = null;
        clearEditor();
    }

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
