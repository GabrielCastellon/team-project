package use_case.edit_boards;

public interface EditBoardInputBoundary {
    void load(String userId, String boardId);
    void save(EditBoardInputData data);
}
