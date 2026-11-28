package view;

import interface_adapter.discovery.DiscoveryPresenter.ThumbnailLoader;
import interface_adapter.view_pins.ViewPinsPresenter.PinCard;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Presentation of a single board inside the Boards tab. */
final class BoardDetailView extends JPanel {
    private static final Color INK = new Color(25, 25, 25);
    private static final Color MUTED = new Color(92, 90, 87);
    private final JLabel title = new JLabel();
    private final JLabel count = new JLabel();
    private final JLabel message = new JLabel("", SwingConstants.CENTER);
    private final JTextField search = new JTextField();
    private final CardLayout pages = new CardLayout();
    private final JPanel content = new JPanel(pages);
    private final PinsView gallery;
    private int total;

    BoardDetailView(ThumbnailLoader thumbnails, Runnable goBack) {
        this(thumbnails, goBack, null);
    }

    BoardDetailView(ThumbnailLoader thumbnails, Runnable goBack, Runnable editBoard) {
        this(thumbnails, goBack, editBoard, editBoard);
    }

    BoardDetailView(ThumbnailLoader thumbnails, Runnable goBack, Runnable editBoard, Runnable addPins) {
        super(new BorderLayout(0, 20));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(24, 32, 24, 32));
        gallery = new PinsView(thumbnails, true);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JButton back = new JButton("‹  Boards") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D p = smooth(g);
                p.setColor(getModel().isRollover() || isFocusOwner() ? new Color(225, 224, 220) : new Color(243, 242, 239));
                p.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                p.dispose();
                super.paintComponent(g);
            }
        };
        back.setContentAreaFilled(false);
        back.setOpaque(false);
        back.setBorder(new EmptyBorder(8, 15, 8, 15));
        back.setBorderPainted(false);
        back.setForeground(INK);
        back.setFont(PhotofyTheme.BUTTON);
        back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        back.getAccessibleContext().setAccessibleName("Back to boards");
        back.addActionListener(e -> goBack.run());
        title.setFont(PhotofyTheme.HEADING.deriveFont(38f));
        title.setForeground(INK);
        title.putClientProperty("html.disable", Boolean.TRUE);
        count.setFont(PhotofyTheme.BODY.deriveFont(14f));
        count.setForeground(MUTED);
        back.setAlignmentX(LEFT_ALIGNMENT);
        title.setAlignmentX(LEFT_ALIGNMENT);
        count.setAlignmentX(LEFT_ALIGNMENT);
        header.add(back);
        header.add(Box.createVerticalStrut(19));
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(count);
        if (editBoard != null) {
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
            actions.setOpaque(false);
            actions.setAlignmentX(LEFT_ALIGNMENT);
            JButton editButton = EditBoardDialog.pill("Edit board", false);
            editButton.addActionListener(event -> editBoard.run());
            JButton addPinsButton = EditBoardDialog.pill("+ Add pins", true);
            addPinsButton.addActionListener(event -> addPins.run());
            actions.add(editButton);
            actions.add(addPinsButton);
            header.add(actions);
        }
        header.add(Box.createVerticalStrut(24));
        header.add(searchBox());
        add(header, BorderLayout.NORTH);

        message.setForeground(INK);
        message.setFont(PhotofyTheme.BUTTON.deriveFont(17f));
        message.setIcon(new Icon() {
            public int getIconWidth() { return 54; }
            public int getIconHeight() { return 54; }
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D p = smooth(g);
                p.translate(x, y);
                p.setColor(new Color(247, 240, 239));
                p.fillOval(0, 0, 54, 54);
                p.setColor(PhotofyTheme.RED);
                p.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                p.drawRoundRect(15, 16, 24, 22, 5, 5);
                p.drawLine(29, 17, 29, 37);
                p.drawLine(29, 26, 38, 26);
                p.dispose();
            }
        });
        message.setIconTextGap(12);
        message.setHorizontalTextPosition(SwingConstants.CENTER);
        message.setVerticalTextPosition(SwingConstants.BOTTOM);
        content.setBackground(Color.WHITE);
        content.add(message, "message");
        content.add(gallery, "pins");
        add(content, BorderLayout.CENTER);

        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
        });
    }

    private JPanel searchBox() {
        JPanel box = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D p = smooth(g);
                p.setColor(search.isFocusOwner() ? INK : new Color(184, 182, 178));
                p.setStroke(new BasicStroke(search.isFocusOwner() ? 2f : 1f));
                p.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 26, 26);
                p.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                p.drawOval(16, 14, 11, 11);
                p.drawLine(26, 24, 30, 28);
                if (search.getText().isEmpty() && !search.isFocusOwner()) {
                    p.setColor(MUTED);
                    p.setFont(search.getFont());
                    p.drawString("Search this board", 40, (getHeight() - p.getFontMetrics().getHeight()) / 2
                            + p.getFontMetrics().getAscent());
                }
                p.dispose();
            }
        };
        box.setOpaque(false);
        box.setAlignmentX(LEFT_ALIGNMENT);
        box.setMaximumSize(new Dimension(440, 44));
        box.setPreferredSize(new Dimension(440, 44));
        box.setBorder(new EmptyBorder(0, 40, 0, 12));
        search.setFont(PhotofyTheme.FIELD);
        search.setOpaque(false);
        search.setBorder(null);
        if (!GraphicsEnvironment.isHeadless()) {
            search.getAccessibleContext().setAccessibleName("Search this board");
        }
        search.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent event) { box.repaint(); }
            public void focusLost(FocusEvent event) { box.repaint(); }
        });
        box.add(search);
        return box;
    }

    private static Graphics2D smooth(Graphics g) {
        Graphics2D p = (Graphics2D) g.create();
        p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        return p;
    }

    void showBoard(String name, List<PinCard> pins) {
        title.setText(name);
        total = pins.size();
        count.setText(total + (total == 1 ? " pin" : " pins"));
        search.setText("");
        gallery.showPins(pins);
        filter();
    }

    private void filter() {
        int matches = gallery.filterPins(search.getText());
        message.setText(total == 0 ? "No saved pins yet" : "No matching pins");
        pages.show(content, matches == 0 ? "message" : "pins");
    }
}
