package view;

import interface_adapter.create_board.BoardsState;
import interface_adapter.create_board.BoardsViewModel;
import interface_adapter.create_board.BoardsViewModelListener;
import interface_adapter.create_board.CreateBoardController;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;

public class BoardsView extends JPanel implements BoardsViewModelListener {
    private final CreateBoardController controller;
    private final BoardsViewModel viewModel;
    private final DefaultListModel<String> boardNames = new DefaultListModel<>();
    private final JButton createButton = new JButton("Create Board");
    private String currentUserId;

    public BoardsView(CreateBoardController controller, BoardsViewModel viewModel) {
        super(new BorderLayout());
        this.controller = controller;
        this.viewModel = viewModel;

        createButton.setEnabled(false);
        createButton.addActionListener(event -> showCreateBoardDialog());

        add(createButton, BorderLayout.NORTH);
        add(new JScrollPane(new JList<>(boardNames)), BorderLayout.CENTER);

        viewModel.addListener(this);
        stateChanged();
    }

    public void setCurrentUserId(String userId) {
        if (!java.util.Objects.equals(currentUserId, userId)) {
            currentUserId = userId;
            viewModel.setState(new BoardsState());
            viewModel.notifyStateChanged();
        }
        createButton.setEnabled(userId != null && !userId.isBlank());
    }

    private void showCreateBoardDialog() {
        String name = JOptionPane.showInputDialog(this, "Enter a board name:");

        if (name != null) {
            controller.execute(currentUserId, name);
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
                    "Create Board", JOptionPane.ERROR_MESSAGE);
        }
    }
}
