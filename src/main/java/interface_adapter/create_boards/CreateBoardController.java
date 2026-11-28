package interface_adapter.create_boards;

import use_case.create_boards.CreateBoardInputBoundary;
import use_case.create_boards.CreateBoardInputData;

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
