package use_case.delete_boards;

public interface DeleteBoardDataAccessInterface {
    boolean deleteOwnedBoard(String userId, String boardId);
}
