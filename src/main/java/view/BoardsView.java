package view;

import interface_adapter.boards.BoardsState;
import interface_adapter.edit_boards.EditBoardController;
import interface_adapter.delete_boards.DeleteBoardController;
import interface_adapter.edit_boards.EditBoardEditorState;
import interface_adapter.boards.BoardsViewModel;
import interface_adapter.boards.BoardsViewModelListener;
import interface_adapter.create_boards.CreateBoardController;
import interface_adapter.view_boards.ViewBoardsController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import interface_adapter.open_boards.OpenBoardController;
import interface_adapter.open_boards.OpenBoardPresenter.OpenedBoard;
import interface_adapter.discovery.DiscoveryPresenter.ThumbnailLoader;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BoardsView extends JPanel implements BoardsViewModelListener {

    private static final int CARD_WIDTH = 240;
    private static final int CARD_HEIGHT = 200;
    private static final int CARD_GAP = 20;

    private final CreateBoardController createBoardController;
    private final ViewBoardsController viewBoardsController;
    private final BoardsViewModel viewModel;

    private final OpenBoardController openBoardController;
    private final EditBoardController editBoardController;
    private final DeleteBoardController deleteBoardController;
    private EditBoardEditorState shownEditor;
    private final ThumbnailLoader editThumbnails;
    private boolean addPinsOnly;
    private List<String> originalEditorPins;
    private final CardLayout screens = new CardLayout();
    private final BoardDetailView detail;
    private OpenedBoard displayedBoard;

    private final JPanel boardsPanel = new JPanel();
    private final JMenu sortMenu = new JMenu("Sort by");
    private final JRadioButtonMenuItem mostPins = new JRadioButtonMenuItem("Most pins");
    private final JButton createButton = new JButton("+ Create board") {
        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = getBackground();
            if (!isEnabled()) {
                fill = PhotofyTheme.CARD;
            } else if (getModel().isPressed() && getModel().isArmed()) {
                fill = fill.darker();
            }
            g.setColor(fill);
            int height = getHeight();
            g.fillRoundRect(0, 0, getWidth(), height, height, height);
            g.dispose();
            super.paintComponent(graphics);
        }
    };

    private String currentUserId;
    private int gridColumns = 3;

    private void updateColumns() {
        GridLayout grid = (GridLayout) boardsPanel.getLayout();
        grid.setColumns(viewModel.getState().getBoards().isEmpty() ? 1 : gridColumns);
    }

    public BoardsView(
            CreateBoardController createBoardController,
            BoardsViewModel viewModel,
            ViewBoardsController viewBoardsController, OpenBoardController openBoardController,
            ThumbnailLoader thumbnails) {
        this(createBoardController, viewModel, viewBoardsController, openBoardController, thumbnails, null);
    }

    public BoardsView(CreateBoardController createBoardController, BoardsViewModel viewModel,
                      ViewBoardsController viewBoardsController, OpenBoardController openBoardController,
                      ThumbnailLoader thumbnails, EditBoardController editBoardController) {
        this(createBoardController, viewModel, viewBoardsController, openBoardController,
                thumbnails, editBoardController, null);
    }

    public BoardsView(CreateBoardController createBoardController, BoardsViewModel viewModel,
                      ViewBoardsController viewBoardsController, OpenBoardController openBoardController,
                      ThumbnailLoader thumbnails, EditBoardController editBoardController,
                      DeleteBoardController deleteBoardController) {
        super();
        this.deleteBoardController = deleteBoardController;
        this.editBoardController = editBoardController;
        this.editThumbnails = thumbnails;
        setLayout(screens);
        this.openBoardController = openBoardController;
        this.detail = new BoardDetailView(thumbnails, () -> {
            if (viewModel.isDeletePending()) {
                return;
            }
            viewModel.getState().clearOpenedBoard();
            viewModel.notifyStateChanged();
        }, editBoardController == null ? null : () -> beginEdit(false),
                editBoardController == null ? null : () -> beginEdit(true));

        this.createBoardController = createBoardController;
        this.viewModel = viewModel;
        this.viewBoardsController = viewBoardsController;

        setBackground(PhotofyTheme.BACKGROUND);

        configureCreateButton();
        configureBoardsPanel();

        JPanel list = new JPanel(new BorderLayout());
        list.add(createHeader(), BorderLayout.NORTH);
        list.add(createScrollPane(), BorderLayout.CENTER);
        add(list, "list");
        add(detail, "detail");

        viewModel.addListener(this);

        addHierarchyListener(event -> {
            if ((event.getChangeFlags()
                    & HierarchyEvent.SHOWING_CHANGED) != 0
                    && isShowing()) {
                onBoardsOpened();
            }
        });

        stateChanged();
    }

    private void configureCreateButton() {
        createButton.setFont(PhotofyTheme.BUTTON);
        createButton.setForeground(Color.WHITE);
        createButton.setBackground(PhotofyTheme.RED);
        createButton.setBorderPainted(false);
        createButton.setContentAreaFilled(false);
        createButton.setOpaque(false);
        createButton.setBorder(new EmptyBorder(12, 20, 12, 20));
        createButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        createButton.setEnabled(false);

        createButton.addActionListener(event ->
                showCreateBoardDialog());
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 12));
        header.setBackground(PhotofyTheme.BACKGROUND);
        header.setBorder(new EmptyBorder(28, 32, 24, 32));

        JLabel title = new JLabel("Your boards");
        title.setFont(PhotofyTheme.HEADING);
        title.setForeground(PhotofyTheme.TEXT);

        JPanel actions = new JPanel(new BorderLayout());
        actions.setOpaque(false);
        JPanel sortPanel = new JPanel(new GridBagLayout());
        sortPanel.setOpaque(false);
        sortPanel.add(createSortMenu());
        actions.add(sortPanel, BorderLayout.WEST);
        actions.add(createButton, BorderLayout.EAST);
        header.add(title, BorderLayout.NORTH);
        header.add(actions, BorderLayout.CENTER);

        return header;
    }

    private JMenuBar createSortMenu() {
        JRadioButtonMenuItem byName = new JRadioButtonMenuItem("Name (A–Z)", true);
        ButtonGroup choices = new ButtonGroup();
        choices.add(byName);
        choices.add(mostPins);
        byName.addActionListener(event -> renderBoards(viewModel.getState()));
        mostPins.addActionListener(event -> renderBoards(viewModel.getState()));

        BufferedImage sliders = new BufferedImage(18, 18, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sliders.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(PhotofyTheme.TEXT);
        g.setStroke(new BasicStroke(1.5f));
        g.drawLine(2, 5, 16, 5);
        g.drawLine(2, 13, 16, 13);
        g.fillOval(9, 2, 6, 6);
        g.fillOval(4, 10, 6, 6);
        g.dispose();

        sortMenu.setIcon(new ImageIcon(sliders));
        sortMenu.setFont(PhotofyTheme.BUTTON);
        sortMenu.setMnemonic('S');
        sortMenu.add(byName);
        sortMenu.add(mostPins);
        JMenuBar bar = new JMenuBar();
        bar.setOpaque(false);
        bar.setBorderPainted(false);
        bar.add(sortMenu);
        return bar;
    }

    private void configureBoardsPanel() {
        boardsPanel.setLayout(
                new GridLayout(0, 3, CARD_GAP, CARD_GAP));
        boardsPanel.setBackground(PhotofyTheme.BACKGROUND);
    }

    private JScrollPane createScrollPane() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(PhotofyTheme.BACKGROUND);
        wrapper.setBorder(new EmptyBorder(0, 32, 32, 32));

        wrapper.add(boardsPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(wrapper);

        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(
                PhotofyTheme.BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.getViewport().addComponentListener(
                new java.awt.event.ComponentAdapter() {
                    @Override
                    public void componentResized(
                            java.awt.event.ComponentEvent event) {

                        int width = scrollPane.getViewport().getWidth() - 64;

                        int columns = Math.max(1, (width + CARD_GAP) / (CARD_WIDTH + CARD_GAP));

                        gridColumns = columns;
                        updateColumns();

                        boardsPanel.revalidate();
                    }
                });

        return scrollPane;
    }

    public void setCurrentUserId(String userId) {
        if (!Objects.equals(currentUserId, userId)) {
            currentUserId = userId;
            viewModel.resetForAccountChange();

            if (isShowing()) {
                onBoardsOpened();
            }
        }

        createButton.setEnabled(
                userId != null && !userId.isBlank());
    }

    private void onBoardsOpened() {
        if (currentUserId != null && !currentUserId.isBlank()) {
            viewBoardsController.execute(currentUserId);
        }
    }

    private void beginEdit(boolean addOnly) {
        BoardsState state = viewModel.getState();
        OpenedBoard board = state.getOpenedBoard();
        if (board != null && currentUserId != null && editBoardController != null
                && state.getEditor() == null && !viewModel.isDeletePending()) {
            addPinsOnly = addOnly;
            originalEditorPins = null;
            editBoardController.load(currentUserId, board.id());
        }
    }

    private void showEditBoardDialog(EditBoardEditorState editor) {
        BoardsState state = viewModel.getState();
        OpenedBoard board = state.getOpenedBoard();
        String owner = currentUserId;
        if (state.getEditor() != editor || board == null || !board.id().equals(editor.boardId())) {
            return;
        }
        if (originalEditorPins == null) {
            originalEditorPins = List.copyOf(editor.selectedPinIds());
        }
        EditBoardDialog dialog = new EditBoardDialog();
        long accountVersion = viewModel.getAccountVersion();
        boolean confirmed = dialog.show(this, editor, addPinsOnly, originalEditorPins,
                editThumbnails, deleteBoardController != null);
        state = viewModel.getState();
        if (!Objects.equals(owner, currentUserId) || state.getEditor() != editor
                || accountVersion != viewModel.getAccountVersion()) {
            return;
        }
        if (dialog.isDeleteRequested() && deleteBoardController != null) {
            state.clearEditor();
            shownEditor = null;
            originalEditorPins = null;
            deleteBoardController.execute(owner, editor.boardId());
            return;
        }
        if (!confirmed) {
            state.clearEditor();
            shownEditor = null;
            return;
        }
        var draft = new EditBoardEditorState(editor.boardId(), dialog.getName(),
                editor.availablePins(), dialog.getSelectedPinIds());
        state.setEditor(draft);
        shownEditor = draft;
        editBoardController.save(owner, editor.boardId(), draft.name(), draft.selectedPinIds());
    }

    private void showCreateBoardDialog() {
        String name = JOptionPane.showInputDialog(
                this,
                "Enter a board name:",
                "Create board",
                JOptionPane.PLAIN_MESSAGE);

        if (name != null) {
            createBoardController.execute(currentUserId, name);
        }
    }

    private JButton createBoardCard(String boardId, String name, int pinCount) {
        JButton card = new JButton();
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(0, 0, 0, 0));
        card.setContentAreaFilled(false);
        card.setBorderPainted(false);
        card.setRolloverEnabled(true);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.getAccessibleContext().setAccessibleName("Open board " + name);
        card.addActionListener(event -> {
            if (!viewModel.isDeletePending()) {
                openBoardController.execute(currentUserId, boardId);
            }
        });

        card.setOpaque(false);
        card.setPreferredSize(
                new Dimension(CARD_WIDTH, CARD_HEIGHT));

        card.add(createBoardCover(card), BorderLayout.CENTER);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(
                PhotofyTheme.BUTTON.deriveFont(16f));
        nameLabel.setForeground(PhotofyTheme.TEXT);
        nameLabel.putClientProperty("html.disable", Boolean.TRUE);

        JLabel countLabel = new JLabel(pinCount + (pinCount == 1 ? " pin" : " pins"));
        countLabel.setFont(PhotofyTheme.BODY);
        countLabel.setForeground(PhotofyTheme.TEXT);

        JPanel caption = new JPanel(new GridLayout(2, 1, 0, 2));
        caption.setOpaque(false);
        caption.setBorder(new EmptyBorder(0, 4, 4, 4));
        caption.add(nameLabel);
        caption.add(countLabel);
        card.add(caption, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createBoardCover(JButton card) {
        return new JPanel() {
            {
                setOpaque(false);
                setPreferredSize(new Dimension(240, 160));

            }

            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);

                Graphics2D g = (Graphics2D) graphics.create();

                g.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();

                g.setClip(new java.awt.geom.RoundRectangle2D.Double(
                        0, 0, width, height, 24, 24));

                int split = (int) (width * 0.65);

                g.setColor(new Color(225, 222, 219));
                g.fillRect(0, 0, split - 2, height);

                g.setColor(new Color(215, 212, 209));
                g.fillRect(split, 0, width - split,
                        height / 2 - 2);

                g.setColor(new Color(233, 230, 227));
                g.fillRect(split, height / 2,
                        width - split, height / 2);

                if (card.getModel().isRollover() || card.isFocusOwner()) {
                    g.setColor(new Color(0, 0, 0, 32));
                    g.fillRect(0, 0, width, height);
                }

                g.dispose();
            }
        };
    }

    private JPanel createEmptyState() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(50, 0, 50, 0));

        boolean signedIn =
                currentUserId != null && !currentUserId.isBlank();

        JLabel title = new JLabel(
                signedIn ? "No boards yet" : "Sign in to view your boards");

        title.setFont(PhotofyTheme.HEADING);
        title.setForeground(PhotofyTheme.TEXT);

        JLabel description = new JLabel(
                signedIn
                        ? "Create your first board to get started."
                        : "Your saved boards will appear here.");

        description.setFont(PhotofyTheme.BODY);
        description.setForeground(Color.GRAY);

        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        description.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(8));
        panel.add(description);

        return panel;
    }

    @Override
    public void stateChanged() {
        BoardsState state = viewModel.getState();
        OpenedBoard board = state.getOpenedBoard();
        if (board == null) {
            if (displayedBoard != null) {
                detail.showBoard("", List.of());
            }
            renderBoards(state);
            screens.show(this, "list");
        } else if (board != displayedBoard) {
            detail.showBoard(board.name(), board.pins());
            screens.show(this, "detail");
        }
        displayedBoard = board;

        if (!state.getErrorMessage().isEmpty()) {
            String error = state.getErrorMessage();
            state.setError("");
            JOptionPane.showMessageDialog(this, error, "Boards", JOptionPane.ERROR_MESSAGE);
        }
        EditBoardEditorState editor = viewModel.getState().getEditor();
        if (editor == null) {
            shownEditor = null;
        } else if (editBoardController != null && editor != shownEditor) {
            shownEditor = editor;
            SwingUtilities.invokeLater(() -> showEditBoardDialog(editor));
        }
    }

    private void renderBoards(BoardsState state) {
        boardsPanel.removeAll();
        updateColumns();
        sortMenu.setEnabled(state.getBoards().size() > 1);

        if (state.getBoards().isEmpty()) {
            boardsPanel.add(createEmptyState());
        } else {
            List<Map.Entry<String, String>> boards = new ArrayList<>(state.getBoards().entrySet());
            Comparator<Map.Entry<String, String>> byName =
                    Map.Entry.comparingByValue(String.CASE_INSENSITIVE_ORDER);
            if (mostPins.isSelected()) {
                Comparator<Map.Entry<String, String>> byCount =
                        Comparator.comparingInt(board -> state.getPinCount(board.getKey()));
                boards.sort(byCount.reversed().thenComparing(byName));
            } else {
                boards.sort(byName);
            }
            for (Map.Entry<String, String> board : boards) {
                boardsPanel.add(createBoardCard(
                        board.getKey(),
                        board.getValue(),
                        state.getPinCount(board.getKey())
                ));
            }
        }

        boardsPanel.revalidate();
        boardsPanel.repaint();

    }
}
