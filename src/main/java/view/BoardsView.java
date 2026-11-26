package view;

import interface_adapter.boards.BoardsState;
import interface_adapter.boards.BoardsViewModel;
import interface_adapter.boards.BoardsViewModelListener;
import interface_adapter.create_boards.CreateBoardController;
import interface_adapter.view_boards.ViewBoardsController;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.event.HierarchyEvent;

public class BoardsView extends JPanel implements BoardsViewModelListener {
    private final CreateBoardController createBoardController;
    private final ViewBoardsController viewBoardsController;
    private final BoardsViewModel viewModel;
    private final DefaultListModel<String> boardNames = new DefaultListModel<>();
    private final JButton createButton = new JButton("Create Board");
    private String currentUserId;

    public BoardsView(CreateBoardController createBoardController, BoardsViewModel viewModel, ViewBoardsController viewBoardsController) {
        super(new BorderLayout());
        this.createBoardController = createBoardController;
        this.viewModel = viewModel;
        this.viewBoardsController = viewBoardsController;

        createButton.setEnabled(false);
        createButton.addActionListener(event -> showCreateBoardDialog());

        add(createButton, BorderLayout.NORTH);
        add(new JScrollPane(new JList<>(boardNames)), BorderLayout.CENTER);

        viewModel.addListener(this);
        addHierarchyListener(event -> {
            if ((event.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                onBoardsOpened();
            }
        });
        stateChanged();
    }

    public void setCurrentUserId(String userId) {
        if (!java.util.Objects.equals(currentUserId, userId)) {
            currentUserId = userId;
            viewModel.resetForAccountChange();
            if (isShowing()) {
                onBoardsOpened();
            }
        }
        createButton.setEnabled(userId != null && !userId.isBlank());
    }

    private void onBoardsOpened() {
        if (currentUserId != null && !currentUserId.isBlank()) {
            viewBoardsController.execute(currentUserId);
        }
    }

    private void showCreateBoardDialog() {
        String name = JOptionPane.showInputDialog(this, "Enter a board name:");

        if (name != null) {
            createBoardController.execute(currentUserId, name);
        }
    }

    @Override
    public void stateChanged() {
        BoardsState state = viewModel.getState();

        boardNames.clear();
        for (String name : state.getBoards().values()) {
            boardNames.addElement(name);
        }

        if (!state.getErrorMessage().isEmpty()) {
            JOptionPane.showMessageDialog(this, state.getErrorMessage(),
                    "Boards", JOptionPane.ERROR_MESSAGE);
        }
    }
}
