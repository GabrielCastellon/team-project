package app;

import interface_adapter.boards.BoardsViewModel;
import interface_adapter.discovery.TaskRunner;
import interface_adapter.view_boards.ViewBoardsPresenter;
import use_case.view_boards.ViewBoardsDataAccessInterface;
import use_case.view_boards.ViewBoardsInputBoundary;
import use_case.view_boards.ViewBoardsInputData;
import use_case.view_boards.ViewBoardsInteractor;
import use_case.view_boards.ViewBoardsOutputBoundary;
import use_case.view_boards.ViewBoardsOutputData;

final class BackgroundBoardRefresh implements ViewBoardsInputBoundary {
    private final ViewBoardsDataAccessInterface boardDAO;
    private final BoardsViewModel viewModel;
    private final ViewBoardsPresenter presenter;
    private final TaskRunner<Runnable> runner;
    private long latestRequest;
    private long savedBoardsVersion;

    BackgroundBoardRefresh(ViewBoardsDataAccessInterface boardDAO, BoardsViewModel viewModel,
                           TaskRunner<Runnable> runner) {
        this.boardDAO = boardDAO;
        this.viewModel = viewModel;
        this.presenter = new ViewBoardsPresenter(viewModel);
        this.runner = runner;
    }

    void boardSaved() {
        savedBoardsVersion++;
    }

    @Override
    public void execute(ViewBoardsInputData inputData) {
        long request = ++latestRequest;
        long accountAtStart = viewModel.getAccountVersion();
        long savesAtStart = savedBoardsVersion;

        runner.run(() -> loadBoards(inputData), (updateView, failure) -> {
            if (request != latestRequest || accountAtStart != viewModel.getAccountVersion()) {
                return;
            }
            if (savesAtStart != savedBoardsVersion) {
                // The read may predate the save. Fetch the complete list again.
                execute(inputData);
            } else if (failure != null) {
                presenter.prepareFailView("Could not load your boards.");
            } else {
                updateView.run();
            }
        });
    }

    private Runnable loadBoards(ViewBoardsInputData inputData) {
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
