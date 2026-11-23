package use_case.view_boards;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ViewBoardsOutputData {
    private final Map<String, String> boards;

    public ViewBoardsOutputData(Map<String, String> boards) {
        this.boards = Collections.unmodifiableMap(new LinkedHashMap<>(boards));
    }

    public Map<String, String> getBoards() {
        return boards;
    }
}
