package app;

import data_access.JsonBoardDataAccessObject;
import entity.BoardFactory;
import interface_adapter.create_board.BoardsViewModel;
import interface_adapter.create_board.CreateBoardController;
import interface_adapter.create_board.CreateBoardPresenter;
import interface_adapter.view_boards.ViewBoardsController;
import interface_adapter.view_boards.ViewBoardsPresenter;
import use_case.create_board.CreateBoardInteractor;
import use_case.create_board.CreateBoardInputBoundary;
import use_case.create_board.CreateBoardInputData;
import use_case.create_board.CreateBoardOutputBoundary;
import use_case.create_board.CreateBoardOutputData;
import use_case.view_boards.ViewBoardsInputData;
import use_case.view_boards.ViewBoardsInteractor;
import use_case.view_boards.ViewBoardsOutputBoundary;
import use_case.view_boards.ViewBoardsOutputData;
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
        ViewBoardsPresenter viewPresenter = new ViewBoardsPresenter(viewModel);
        JsonBoardDataAccessObject boardDAO = new JsonBoardDataAccessObject(file);
        BoardFactory boardFactory = new BoardFactory();
        SwingTaskRunner<Runnable> runner = new SwingTaskRunner<>();

        CreateBoardInputBoundary backgroundCreate = new CreateBoardInputBoundary() {
            private boolean saving;

            @Override
            public void execute(CreateBoardInputData inputData) {
                if (saving) {
                    return;
                }
                saving = true;
                var originalState = viewModel.getState();

                runner.run(() -> {
                    class BoardResult implements CreateBoardOutputBoundary {
                        private Runnable showResult;

                        @Override
                        public void prepareSuccessView(CreateBoardOutputData data) {
                            showResult = () -> presenter.prepareSuccessView(data);
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
                    if (viewModel.getState() != originalState) {
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

        ViewBoardsController viewController = new ViewBoardsController(inputData -> {
            var stateAtRequest = viewModel.getState();

            runner.run(() -> loadBoards(boardDAO, inputData, viewPresenter),
                    (updateView, failure) -> {
                        if (viewModel.getState() != stateAtRequest) {
                            return;
                        }
                        if (failure != null) {
                            viewPresenter.prepareFailView("Could not load your boards.");
                        } else {
                            updateView.run();
                        }
                    });
        });
        return new BoardsView(new CreateBoardController(backgroundCreate), viewModel, viewController);
    }
    private static Runnable loadBoards(
            JsonBoardDataAccessObject boardDAO,
            ViewBoardsInputData inputData,
            ViewBoardsPresenter presenter) {

        class LoadResult implements ViewBoardsOutputBoundary {
            private Runnable updateView;

            @Override
            public void prepareSuccessView(ViewBoardsOutputData data) {
                updateView = () -> presenter.prepareSuccessView(data);
            }

            @Override
            public void prepareFailView(String error) {
                updateView = () -> presenter.prepareFailView(error);
            }
        }

        LoadResult result = new LoadResult();
        new ViewBoardsInteractor(boardDAO, result).execute(inputData);
        return result.updateView;
    }
}
