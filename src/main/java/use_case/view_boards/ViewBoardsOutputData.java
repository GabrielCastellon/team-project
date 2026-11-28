package use_case.view_boards;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ViewBoardsOutputData {
    private final Map<String, String> boards;

    private final Map<String, Integer> pinCounts;

    public ViewBoardsOutputData(Map<String, String> boards) {
        this(boards, Map.of());
    }

    public ViewBoardsOutputData(Map<String, String> boards, Map<String, Integer> pinCounts) {
        this.pinCounts = Map.copyOf(pinCounts);
        this.boards = Collections.unmodifiableMap(new LinkedHashMap<>(boards));
    }

    public int getPinCount(String boardId) {
        return pinCounts.getOrDefault(boardId, 0);
    }

    public Map<String, String> getBoards() {
        return boards;
    }
}
