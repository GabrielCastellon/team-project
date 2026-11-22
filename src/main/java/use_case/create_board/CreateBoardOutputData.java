package use_case.create_board;

public class CreateBoardOutputData {
    private final String boardId;
    private final String boardName;

    public CreateBoardOutputData(String boardId, String boardName) {
        this.boardId = boardId;
        this.boardName = boardName;
    }

    public String getBoardId() {
        return boardId;
    }

    public String getBoardName() {
        return boardName;
    }
}
