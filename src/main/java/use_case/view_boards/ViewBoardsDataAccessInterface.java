package use_case.view_boards;

import entity.Board;
import java.util.List;

public interface ViewBoardsDataAccessInterface {
    List<Board> findByUserId(String userId);
}
