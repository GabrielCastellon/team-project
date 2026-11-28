package use_case.create_board;

public class CreateBoardInputData {
    private final String userId;
    private final String name;

    public CreateBoardInputData(String userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }
}
