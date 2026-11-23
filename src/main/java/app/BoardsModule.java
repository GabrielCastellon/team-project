package app;

import data_access.JsonBoardDataAccessObject;
import entity.BoardFactory;
import interface_adapter.create_board.BoardsViewModel;
import interface_adapter.create_board.CreateBoardController;
import interface_adapter.create_board.CreateBoardPresenter;
import use_case.create_board.CreateBoardInteractor;
import view.BoardsView;

import java.nio.file.Path;

public final class BoardsModule {
    private BoardsModule() {
    }

    public static BoardsView createPanel() {
        Path file = Path.of(System.getProperty("user.home"), ".photofy", "boards.json");

        BoardsViewModel viewModel = new BoardsViewModel();
        CreateBoardPresenter presenter = new CreateBoardPresenter(viewModel);
        JsonBoardDataAccessObject boardDAO = new JsonBoardDataAccessObject(file);
        CreateBoardInteractor interactor =
                new CreateBoardInteractor(boardDAO, presenter, new BoardFactory());
        CreateBoardController controller = new CreateBoardController(interactor);

        return new BoardsView(controller, viewModel);
    }
}
