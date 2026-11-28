package view;

import interface_adapter.boards.BoardsState;
import interface_adapter.boards.BoardsViewModel;
import interface_adapter.boards.BoardsViewModelListener;
import interface_adapter.create_board.CreateBoardController;
import interface_adapter.view_boards.ViewBoardsController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

public class BoardsView extends JPanel implements BoardsViewModelListener {

    private static final int CARD_WIDTH = 240;
    private static final int CARD_HEIGHT = 200;
    private static final int CARD_GAP = 20;

    private final CreateBoardController createBoardController;
    private final ViewBoardsController viewBoardsController;
    private final BoardsViewModel viewModel;

    private final JPanel boardsPanel = new JPanel();
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
            ViewBoardsController viewBoardsController) {

        super(new BorderLayout());

        this.createBoardController = createBoardController;
        this.viewModel = viewModel;
        this.viewBoardsController = viewBoardsController;

        setBackground(PhotofyTheme.BACKGROUND);

        configureCreateButton();
        configureBoardsPanel();

        add(createHeader(), BorderLayout.NORTH);
        add(createScrollPane(), BorderLayout.CENTER);

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
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PhotofyTheme.BACKGROUND);
        header.setBorder(new EmptyBorder(28, 32, 24, 32));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(
                new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel title = new JLabel("Your boards");
        title.setFont(PhotofyTheme.HEADING);
        title.setForeground(PhotofyTheme.TEXT);

        textPanel.add(title);

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(createButton);

        header.add(textPanel, BorderLayout.WEST);
        header.add(buttonPanel, BorderLayout.EAST);

        return header;
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

                        int width = scrollPane.getViewport()
                                .getWidth() - 64;

                        int columns = Math.max(
                                1,
                                (width + CARD_GAP)
                                        / (CARD_WIDTH + CARD_GAP));

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

    private JPanel createBoardCard(String name, int pinCount) {
        JPanel card = new JPanel(new BorderLayout(0, 8));

        card.setOpaque(false);
        card.setPreferredSize(
                new Dimension(CARD_WIDTH, CARD_HEIGHT));

        card.add(createBoardCover(), BorderLayout.CENTER);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(
                PhotofyTheme.BUTTON.deriveFont(16f));
        nameLabel.setForeground(PhotofyTheme.TEXT);
        nameLabel.putClientProperty("html.disable", Boolean.TRUE);
        nameLabel.setToolTipText(name);

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

    private JPanel createBoardCover() {
        return new JPanel() {
            private boolean hovered;

            {
                setOpaque(false);
                setPreferredSize(new Dimension(240, 160));
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent event) {
                        hovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent event) {
                        hovered = false;
                        repaint();
                    }
                });
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

                if (hovered) {
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

        boardsPanel.removeAll();
        updateColumns();

        if (state.getBoards().isEmpty()) {
            boardsPanel.add(createEmptyState());
        } else {
            state.getBoards().forEach((id, name) ->
                    boardsPanel.add(createBoardCard(name, state.getPinCount(id))));
        }

        boardsPanel.revalidate();
        boardsPanel.repaint();

        if (!state.getErrorMessage().isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    state.getErrorMessage(),
                    "Boards",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
