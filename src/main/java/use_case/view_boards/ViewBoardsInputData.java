package use_case.view_boards;

public class ViewBoardsInputData {
    private final String userId;

    public ViewBoardsInputData(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
