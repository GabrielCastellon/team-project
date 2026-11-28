package view;

import interface_adapter.discovery.DiscoveryPresenter.ThumbnailLoader;
import interface_adapter.edit_boards.EditBoardEditorState;
import interface_adapter.edit_boards.EditBoardEditorState.PinChoice;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class EditBoardDialog {
    private String name;
    private boolean deleteRequested;

    public boolean isDeleteRequested() {
        return deleteRequested;
    }

    private void confirmDelete(JDialog dialog) {
        Object[] options = {"Delete board", "Cancel"};
        int answer = JOptionPane.showOptionDialog(dialog,
                "Permanently delete this board? Your saved pins will not be deleted.",
                "Delete this board?", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE, null, options, options[1]);
        if (answer == JOptionPane.YES_OPTION) {
            deleteRequested = true;
            dialog.dispose();
        }
    }
    private List<String> selectedPinIds = List.of();

    static JButton pill(String text, boolean primary) {
        JButton button = new JButton(text) {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = isEnabled() ? getBackground() : new Color(235, 233, 230);
                if (isEnabled() && (getModel().isRollover() || getModel().isPressed())) {
                    fill = fill.darker();
                }
                g.setColor(fill);
                g.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        button.setBackground(primary ? PhotofyTheme.RED : new Color(241, 239, 236));
        button.setForeground(primary ? Color.WHITE : PhotofyTheme.TEXT);
        button.setFont(PhotofyTheme.BUTTON);
        button.setBorder(new EmptyBorder(11, 19, 11, 19));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    boolean show(Component parent, EditBoardEditorState state, boolean addOnly,
                 List<String> originalPins, ThumbnailLoader thumbnails) {
        return show(parent, state, addOnly, originalPins, thumbnails, false);
    }

    boolean show(Component parent, EditBoardEditorState state, boolean addOnly,
                 List<String> originalPins, ThumbnailLoader thumbnails, boolean allowDelete) {
        deleteRequested = false;
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent),
                addOnly ? "Add pins" : "Edit board", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(24, 26, 24, 26));
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(addOnly ? "Add pins" : "Edit board");
        title.setFont(PhotofyTheme.HEADING);
        title.setForeground(PhotofyTheme.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        heading.add(title);
        heading.add(Box.createVerticalStrut(12));
        JTextField nameField = new JTextField(state.name(), 26);
        nameField.setFont(PhotofyTheme.FIELD);
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(205, 203, 200)),
                new EmptyBorder(10, 12, 10, 12)));
        if (!addOnly) {
            JLabel label = new JLabel("Board name");
            label.setFont(PhotofyTheme.BUTTON);
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            heading.add(label);
            heading.add(Box.createVerticalStrut(6));
            nameField.setAlignmentX(Component.LEFT_ALIGNMENT);
            nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
            heading.add(nameField);
        } else {
            JLabel subtitle = new JLabel("Add saved pins to " + state.name());
            subtitle.putClientProperty("html.disable", Boolean.TRUE);
            subtitle.setFont(PhotofyTheme.BODY);
            subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            heading.add(subtitle);
        }
        panel.add(heading, BorderLayout.NORTH);

        JPanel pinList = new JPanel(new GridLayout(0, 2, 10, 10));
        pinList.setOpaque(false);
        Map<String, JCheckBox> choices = new LinkedHashMap<>();
        Map<PinChoice, JLabel> images = new LinkedHashMap<>();
        for (PinChoice pin : state.availablePins()) {
            if (addOnly && originalPins.contains(pin.pinId())) {
                continue;
            }
            JPanel card = new JPanel(new BorderLayout(0, 6));
            card.setBackground(new Color(248, 246, 243));
            card.setBorder(new EmptyBorder(8, 8, 8, 8));
            JLabel image = new JLabel("Photo", SwingConstants.CENTER);
            image.setPreferredSize(new Dimension(184, 116));
            image.setOpaque(true);
            image.setBackground(new Color(231, 228, 224));
            image.setForeground(Color.GRAY);
            images.put(pin, image);
            String description = pin.description() == null ? "Untitled photo" : pin.description();
            JCheckBox checkbox = new JCheckBox(description, state.selectedPinIds().contains(pin.pinId()));
            checkbox.putClientProperty("html.disable", Boolean.TRUE);
            checkbox.setFont(PhotofyTheme.BODY);
            checkbox.setOpaque(false);
            checkbox.getAccessibleContext().setAccessibleName(description);
            checkbox.setMaximumSize(new Dimension(184, 28));
            choices.put(pin.pinId(), checkbox);
            card.add(image, BorderLayout.CENTER);
            card.add(checkbox, BorderLayout.SOUTH);
            pinList.add(card);
        }
        JPanel selection = new JPanel(new BorderLayout(0, 8));
        selection.setOpaque(false);
        JLabel label = new JLabel(addOnly ? "Available saved pins" : "Select pins for this board");
        label.setFont(PhotofyTheme.BUTTON);
        selection.add(label, BorderLayout.NORTH);
        if (choices.isEmpty()) {
            JPanel empty = new JPanel(new GridBagLayout());
            empty.setBackground(new Color(248, 246, 243));
            empty.setPreferredSize(new Dimension(480, 170));
            JLabel message = new JLabel(state.availablePins().isEmpty()
                    ? "Save photos in Discovery to add them here."
                    : "All your saved pins are already in this board.");
            message.setFont(PhotofyTheme.BODY);
            message.setForeground(new Color(95, 93, 90));
            empty.add(message);
            selection.add(empty, BorderLayout.CENTER);
        } else {
            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setBackground(Color.WHITE);
            wrapper.add(pinList, BorderLayout.NORTH);
            JScrollPane scroll = new JScrollPane(wrapper);
            scroll.setBorder(null);
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scroll.setPreferredSize(new Dimension(480, 310));
            selection.add(scroll, BorderLayout.CENTER);
        }
        panel.add(selection, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton cancel = pill("Cancel", false);
        JButton save = pill(addOnly ? "Add selected pins" : "Save changes", true);
        save.setEnabled(!addOnly || !choices.isEmpty());
        if (addOnly) {
            Runnable update = () -> save.setEnabled(choices.values().stream().anyMatch(JCheckBox::isSelected));
            choices.values().forEach(choice -> choice.addItemListener(event -> update.run()));
            update.run();
        }
        boolean[] confirmed = {false};
        cancel.addActionListener(event -> dialog.dispose());
        save.addActionListener(event -> {
            name = addOnly ? state.name() : nameField.getText();
            List<String> selected = new ArrayList<>();
            for (String id : originalPins) {
                JCheckBox choice = choices.get(id);
                if (addOnly || (choice != null && choice.isSelected())) {
                    selected.add(id);
                }
            }
            choices.forEach((id, checkbox) -> {
                if (checkbox.isSelected() && !selected.contains(id)) {
                    selected.add(id);
                }
            });
            selectedPinIds = List.copyOf(selected);
            confirmed[0] = true;
            dialog.dispose();
        });
        actions.add(cancel);
        actions.add(save);
        JPanel footer = new JPanel(new BorderLayout(8, 0));
        footer.setOpaque(false);
        if (!addOnly && allowDelete) {
            JButton delete = pill("Delete board", false);
            delete.setForeground(PhotofyTheme.RED);
            delete.addActionListener(event -> confirmDelete(dialog));
            footer.add(delete, BorderLayout.WEST);
        }
        footer.add(actions, BorderLayout.EAST);
        panel.add(footer, BorderLayout.SOUTH);
        dialog.setContentPane(panel);
        dialog.getRootPane().setDefaultButton(save);
        dialog.getRootPane().registerKeyboardAction(event -> dialog.dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(540, dialog.getHeight()));
        dialog.setLocationRelativeTo(parent);
        SwingWorker<Void, Map.Entry<JLabel, ImageIcon>> worker = new SwingWorker<>() {
            @Override protected Void doInBackground() {
                if (thumbnails == null) {
                    return null;
                }
                for (var entry : images.entrySet()) {
                    if (isCancelled()) {
                        break;
                    }
                    try {
                        BufferedImage photo = thumbnails.load(entry.getKey().imageUrl());
                        if (photo != null) {
                            double scale = Math.min(184.0 / photo.getWidth(), 116.0 / photo.getHeight());
                            int width = Math.max(1, (int) (photo.getWidth() * scale));
                            int height = Math.max(1, (int) (photo.getHeight() * scale));
                            BufferedImage preview = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                            Graphics2D g = preview.createGraphics();
                            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                            g.drawImage(photo, 0, 0, width, height, null);
                            g.dispose();
                            publish(Map.entry(entry.getValue(), new ImageIcon(preview)));
                        }
                    } catch (Exception unavailable) {
                        // The pin remains selectable when its image is unavailable.
                    }
                }
                return null;
            }
            @Override protected void process(List<Map.Entry<JLabel, ImageIcon>> updates) {
                if (!dialog.isDisplayable()) {
                    return;
                }
                updates.forEach(update -> {
                    update.getKey().setText("");
                    update.getKey().setIcon(update.getValue());
                });
            }
        };
        worker.execute();
        dialog.setVisible(true);
        worker.cancel(true);
        return confirmed[0];
    }

    String getName() { return name; }
    List<String> getSelectedPinIds() { return selectedPinIds; }
}
