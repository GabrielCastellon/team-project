package use_case.create_boards;

import entity.Board;

public interface BoardDataAccessInterface {
    boolean existsByName(String userId, String name);
    void save(Board board);
}
