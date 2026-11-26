package app;

import data_access.JsonBoardDataAccessObject;
import entity.BoardFactory;
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

    public static BoardsView createPanel() {
        Path file = Path.of(System.getProperty("user.home"), ".photofy", "boards.json");

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

        ViewBoardsController viewController = new ViewBoardsController(refresh);
        return new BoardsView(new CreateBoardController(backgroundCreate), viewModel, viewController);
    }
}
