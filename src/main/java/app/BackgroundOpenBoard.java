package app;

import interface_adapter.boards.BoardsViewModel;
import interface_adapter.discovery.TaskRunner;
import use_case.open_boards.*;
import use_case.view_pins.ViewPinsDataAccessInterface;

/** Read JSON off the EDT and apply only the latest selection for the active account. */
final class BackgroundOpenBoard implements OpenBoardInputBoundary {
    private final OpenBoardDataAccessInterface boards;
    private final ViewPinsDataAccessInterface pins;
    private final OpenBoardOutputBoundary presenter;
    private final BoardsViewModel model;
    private final TaskRunner<Runnable> runner;
    private long latestSelection;

    BackgroundOpenBoard(OpenBoardDataAccessInterface boards, ViewPinsDataAccessInterface pins,
                        OpenBoardOutputBoundary presenter, BoardsViewModel model, TaskRunner<Runnable> runner) {
        this.boards = boards;
        this.pins = pins;
        this.presenter = presenter;
        this.model = model;
        this.runner = runner;
    }

    @Override
    public void execute(OpenBoardInputData data) {
        long selection = ++latestSelection;
        long account = model.getAccountVersion();
        runner.run(() -> read(data), (update, failure) -> {
            if (selection != latestSelection || account != model.getAccountVersion()) {
                return;
            }
            if (failure == null) {
                update.run();
            } else {
                presenter.prepareFailView("Could not load this board.");
            }
        });
    }

    private Runnable read(OpenBoardInputData data) {
        class Result implements OpenBoardOutputBoundary {
            private Runnable update;

            public void prepareSuccessView(OpenBoardOutputData board) {
                update = () -> presenter.prepareSuccessView(board);
            }

            public void prepareBoardNotFoundView() {
                update = presenter::prepareBoardNotFoundView;
            }

            public void prepareFailView(String message) {
                update = () -> presenter.prepareFailView(message);
            }
        }
        Result result = new Result();
        new OpenBoardInteractor(boards, pins, result).execute(data);
        return result.update;
    }
}
