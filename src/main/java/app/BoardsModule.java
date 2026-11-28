package app;

import data_access.JsonBoardDataAccessObject;
import entity.BoardFactory;
import entity.SessionManager;
import entity.User;
import java.awt.event.HierarchyEvent;
import interface_adapter.create_board.BoardsViewModel;
import interface_adapter.create_board.CreateBoardController;
import interface_adapter.create_board.CreateBoardPresenter;
import use_case.create_board.CreateBoardInteractor;
import use_case.create_board.CreateBoardInputBoundary;
import use_case.create_board.CreateBoardInputData;
import use_case.create_board.CreateBoardOutputBoundary;
import use_case.create_board.CreateBoardOutputData;
import view.SwingTaskRunner;
import view.BoardsView;

import javax.swing.*;
import java.nio.file.Path;

public final class BoardsModule {
    private BoardsModule() {
    }

    public static JPanel createPanel(SessionManager sessionManager) {
        Path file = Path.of("data", "boards.json");
        BoardsViewModel viewModel = new BoardsViewModel();
        CreateBoardPresenter presenter = new CreateBoardPresenter(viewModel);
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

        CreateBoardController controller =
                new CreateBoardController(backgroundCreate);

        BoardsView boardsView = new BoardsView(controller, viewModel);
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
