package app;

import interface_adapter.boards.BoardsViewModel;
import interface_adapter.discovery.TaskRunner;
import use_case.edit_boards.*;
import use_case.open_boards.OpenBoardOutputData;
import use_case.view_boards.ViewBoardsInputData;
import use_case.view_pins.ViewPinsDataAccessInterface;
import java.util.function.Consumer;

final class BackgroundEditBoard implements EditBoardInputBoundary {
    private final EditBoardDataAccessInterface boards;
    private final ViewPinsDataAccessInterface pins;
    private final EditBoardOutputBoundary presenter;
    private final BoardsViewModel model;
    private final TaskRunner<Result> runner;
    private final BackgroundBoardRefresh refresh;
    private boolean busy;

    record Result(Runnable updateView, boolean saved) { }

    BackgroundEditBoard(EditBoardDataAccessInterface boards, ViewPinsDataAccessInterface pins,
                        EditBoardOutputBoundary presenter, BoardsViewModel model,
                        TaskRunner<Result> runner, BackgroundBoardRefresh refresh) {
        this.boards = boards;
        this.pins = pins;
        this.presenter = presenter;
        this.model = model;
        this.runner = runner;
        this.refresh = refresh;
    }

    @Override
    public void load(String userId, String boardId) {
        execute(userId, boardId, interactor -> interactor.load(userId, boardId));
    }

    @Override
    public void save(EditBoardInputData data) {
        execute(data.userId(), data.boardId(), interactor -> interactor.save(data));
    }

    private void execute(String userId, String boardId, Consumer<EditBoardInteractor> operation) {
        if (busy) {
            return;
        }
        busy = true;
        long account = model.getAccountVersion();
        runner.run(() -> perform(operation), (result, failure) -> {
            busy = false;
            if (result != null && result.saved()) {
                refresh.boardSaved();
            }
            if (account != model.getAccountVersion()) {
                return;
            }
            var opened = model.getState().getOpenedBoard();
            if (opened == null || !opened.id().equals(boardId)) {
                if (result != null && result.saved()) {
                    refresh.execute(new ViewBoardsInputData(userId));
                }
                return;
            }
            if (failure != null || result == null || result.updateView() == null) {
                presenter.presentError("Could not edit the board.");
            } else {
                result.updateView().run();
            }
        });
    }

    private Result perform(Consumer<EditBoardInteractor> operation) {
        class EditResult implements EditBoardOutputBoundary {
            private Runnable update;
            private boolean saved;

            public void presentEditor(EditBoardEditorData data) {
                update = () -> presenter.presentEditor(data);
            }

            public void presentSaved(OpenBoardOutputData data) {
                saved = true;
                update = () -> presenter.presentSaved(data);
            }

            public void presentBoardNotFound() {
                update = presenter::presentBoardNotFound;
            }

            public void presentError(String message) {
                update = () -> presenter.presentError(message);
            }
        }
        EditResult result = new EditResult();
        operation.accept(new EditBoardInteractor(boards, pins, result));
        return new Result(result.update, result.saved);
    }
}
