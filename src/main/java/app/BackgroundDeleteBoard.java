package app;

import interface_adapter.boards.BoardsViewModel;
import interface_adapter.discovery.TaskRunner;
import use_case.delete_boards.DeleteBoardDataAccessInterface;
import use_case.delete_boards.DeleteBoardInputBoundary;
import use_case.delete_boards.DeleteBoardInputData;
import use_case.delete_boards.DeleteBoardInteractor;
import use_case.delete_boards.DeleteBoardOutputBoundary;
import use_case.delete_boards.DeleteBoardOutputData;

/** Runs storage operations off the EDT and ignores stale account results. */
final class BackgroundDeleteBoard implements DeleteBoardInputBoundary {
    private final DeleteBoardDataAccessInterface boardDAO;
    private final DeleteBoardOutputBoundary presenter;
    private final BoardsViewModel model;
    private final TaskRunner<Result> runner;
    private final BackgroundBoardRefresh refresh;

    record Result(Runnable updateView, boolean deleted) {
    }

    BackgroundDeleteBoard(DeleteBoardDataAccessInterface boardDAO,
                          DeleteBoardOutputBoundary presenter,
                          BoardsViewModel model, TaskRunner<Result> runner,
                          BackgroundBoardRefresh refresh) {
        this.boardDAO = boardDAO;
        this.presenter = presenter;
        this.model = model;
        this.runner = runner;
        this.refresh = refresh;
    }

    @Override
    public void execute(DeleteBoardInputData data) {
        if (model.isDeletePending()) {
            return;
        }
        model.setDeletePending(true);
        long account = model.getAccountVersion();
        runner.run(() -> perform(data), (result, failure) -> {
            if (result != null && result.deleted()) {
                refresh.boardSaved();
            }
            if (account != model.getAccountVersion()) {
                return;
            }
            model.setDeletePending(false);
            if (failure != null || result == null || result.updateView() == null) {
                presenter.prepareFailView("Could not delete the board.");
            } else {
                result.updateView().run();
            }
        });
    }

    private Result perform(DeleteBoardInputData data) {
        class DeleteResult implements DeleteBoardOutputBoundary {
            private Runnable update;
            private boolean deleted;

            @Override
            public void prepareSuccessView(DeleteBoardOutputData outputData) {
                deleted = true;
                update = () -> presenter.prepareSuccessView(outputData);
            }

            @Override
            public void prepareFailView(String error) {
                update = () -> presenter.prepareFailView(error);
            }
        }
        DeleteResult result = new DeleteResult();
        new DeleteBoardInteractor(boardDAO, result).execute(data);
        return new Result(result.update, result.deleted);
    }
}
