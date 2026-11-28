package interface_adapter.view_boards;

import org.junit.jupiter.api.Test;
import use_case.view_boards.ViewBoardsInputData;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ViewBoardsControllerTest {
    @Test
    void passesUserIdToUseCase() {
        List<ViewBoardsInputData> requests = new ArrayList<>();
        ViewBoardsController controller = new ViewBoardsController(requests::add);

        controller.execute("alice");

        assertEquals(1, requests.size());
        assertEquals("alice", requests.get(0).getUserId());
    }
}
