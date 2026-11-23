package interface_adapter.create_board;

import org.junit.jupiter.api.Test;
import use_case.create_board.CreateBoardInputData;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CreateBoardControllerTest {
    @Test
    void passesUserIdAndBoardNameToUseCase() {
        List<CreateBoardInputData> requests = new ArrayList<>();
        CreateBoardController controller = new CreateBoardController(requests::add);

        controller.execute("u17", "Gym");

        assertEquals(1, requests.size());
        assertEquals("u17", requests.get(0).getUserId());
        assertEquals("Gym", requests.get(0).getName());
    }
}
