package app;

import data_access.JsonBoardDataAccessObject;
import data_access.JsonPinDataAccessObject;
import data_access.ImageLoader;
import interface_adapter.open_boards.OpenBoardController;
import interface_adapter.open_boards.OpenBoardPresenter;
import entity.BoardFactory;
import interface_adapter.edit_boards.EditBoardController;
import interface_adapter.edit_boards.EditBoardPresenter;
import entity.SessionManager;
import entity.User;
import java.awt.event.HierarchyEvent;
import interface_adapter.boards.BoardsViewModel;
import interface_adapter.create_boards.CreateBoardController;
import interface_adapter.create_boards.CreateBoardPresenter;
import interface_adapter.view_boards.ViewBoardsController;
import use_case.create_boards.CreateBoardInteractor;
import use_case.create_boards.CreateBoardInputBoundary;
import use_case.create_boards.CreateBoardInputData;
import use_case.create_boards.CreateBoardOutputBoundary;
import use_case.create_boards.CreateBoardOutputData;
import view.SwingTaskRunner;
import view.BoardsView;

import java.nio.file.Path;

public final class BoardsModule {
    private BoardsModule() {
    }

    public static BoardsView createPanel(SessionManager sessionManager) {
        return createPanel(sessionManager, Path.of("data", "boards.json"), Path.of("data", "pins.json"));
    }

    static BoardsView createPanel(SessionManager sessionManager, Path file, Path pinsFile) {
        BoardsViewModel viewModel = new BoardsViewModel();
        CreateBoardPresenter presenter = new CreateBoardPresenter(viewModel);
        JsonBoardDataAccessObject boardDAO = new JsonBoardDataAccessObject(file);
        BoardFactory boardFactory = new BoardFactory();
        SwingTaskRunner<Runnable> runner = new SwingTaskRunner<>();
        BackgroundBoardRefresh refresh = new BackgroundBoardRefresh(boardDAO, viewModel, runner);

        CreateBoardInputBoundary backgroundCreate = new CreateBoardInputBoundary() {
            private boolean saving;

            @Override
            public void execute(CreateBoardInputData inputData) {
                if (saving) {
                    return;
                }
                saving = true;
                long accountVersionAtStart = viewModel.getAccountVersion();

                runner.run(() -> {
                    class BoardResult implements CreateBoardOutputBoundary {
                        private Runnable showResult;

                        @Override
                        public void prepareSuccessView(CreateBoardOutputData data) {
                            showResult = () -> {
                                refresh.boardSaved();
                                presenter.prepareSuccessView(data);
                            };
                        }

                        @Override
                        public void prepareFailView(String error) {
                            showResult = () -> presenter.prepareFailView(error);
                        }
                    }

                    BoardResult result = new BoardResult();
                    new CreateBoardInteractor(boardDAO, result, boardFactory).execute(inputData);
                    return result.showResult;
                }, (showResult, failure) -> {
                    saving = false;
                    if (viewModel.getAccountVersion() != accountVersionAtStart) {
                        return;
                    }
                    if (failure != null) {
                        presenter.prepareFailView("Could not save the board.");
                    } else {
                        showResult.run();
                    }
                });
            }
        };

        CreateBoardController controller =
                new CreateBoardController(backgroundCreate);

        ViewBoardsController viewController =
                new ViewBoardsController(refresh);

        OpenBoardController openController = new OpenBoardController(new BackgroundOpenBoard(
                boardDAO, new JsonPinDataAccessObject(pinsFile), new OpenBoardPresenter(viewModel), viewModel, runner));
        EditBoardController editController = new EditBoardController(new BackgroundEditBoard(
                boardDAO, new JsonPinDataAccessObject(pinsFile), new EditBoardPresenter(viewModel),
                viewModel, new SwingTaskRunner<>(), refresh));
        BoardsView boardsView = new BoardsView(controller, viewModel, viewController, openController,
                new ImageLoader()::load, editController);

        boardsView.addHierarchyListener(event -> {
            boolean showingChanged =
                    (event.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0;

            if (showingChanged && boardsView.isShowing()) {
                User user = sessionManager.getCurrentUser();

                if (user != null) {
                    boardsView.setCurrentUserId(user.getId());
                } else {
                    boardsView.setCurrentUserId(null);
                }
            }
        });

        return boardsView;
    }
}
