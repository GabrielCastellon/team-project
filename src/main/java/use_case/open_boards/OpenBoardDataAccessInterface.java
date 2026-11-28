package use_case.open_boards;

import entity.Board;
import java.util.Optional;

public interface OpenBoardDataAccessInterface {
    Optional<Board> findByOwnerAndId(String userId, String boardId);
}
