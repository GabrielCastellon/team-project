package interface_adapter.create_board;

import use_case.create_board.CreateBoardInputBoundary;
import use_case.create_board.CreateBoardInputData;

public class CreateBoardController {
    private final CreateBoardInputBoundary inputBoundary;

    public CreateBoardController(CreateBoardInputBoundary inputBoundary) {
        this.inputBoundary = inputBoundary;
    }

    public void execute(String userId, String name) {
        CreateBoardInputData inputData = new CreateBoardInputData(userId, name);
        inputBoundary.execute(inputData);
    }
}
