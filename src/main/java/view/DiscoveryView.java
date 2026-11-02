package view;

import interface_adapter.discovery.DiscoveryController;
import interface_adapter.discovery.DiscoveryController.Card;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.*;

public final class DiscoveryView extends JPanel implements DiscoveryController.View {

    private final JTextField query = new JTextField("bedroom", 17);

    private final JTextField a = new JTextField("board:inspiration-a", 17);

    private final JTextField b = new JTextField("board:inspiration-b", 17);

    private final JTextField group = new JTextField("board:inspiration-a,board:inspiration-b", 30);

    private final JComboBox<String> orientation =
            new JComboBox<>(new String[] {"", "landscape", "portrait", "squarish"});

    private final JComboBox<String> order = new JComboBox<>(new String[] {"relevant", "latest"});

    private final JSpinner page = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));

    private final JSlider weight = new JSlider(0, 100, 70);

    private final JLabel status =
            new JLabel("Search, then like images into A/B to build preferences.");

    private final JPanel controls = new JPanel();

    private final JPanel grid = new JPanel(new GridLayout(0, 3, 12, 12));

    private DiscoveryController controller;

    public DiscoveryView(String model) {
        super(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.add(new JLabel("Image discovery • " + model));
        JPanel search = row();
        search.add(new JLabel("Search"));
        search.add(query);
        search.add(new JLabel("Orientation"));
        search.add(orientation);
        search.add(order);
        search.add(new JLabel("Page"));
        search.add(page);
        search.add(
                button(
                        "Search",
                        () ->
                                controller.search(
                                        query.getText(),
                                        (Integer) page.getValue(),
                                        (String) orientation.getSelectedItem(),
                                        (String) order.getSelectedItem())));
        controls.add(search);
        JPanel profiles = row();
        profiles.add(new JLabel("Preference A"));
        profiles.add(a);
        profiles.add(new JLabel("Preference B"));
        profiles.add(b);
        profiles.add(button("Show A", () -> controller.samples(a.getText())));
        profiles.add(button("Show B", () -> controller.samples(b.getText())));
        controls.add(profiles);
        JPanel ranking = row();
        ranking.add(button("Recommend A", () -> controller.recommend(a.getText())));
        weight.setMajorTickSpacing(25);
        weight.setPaintLabels(true);
        weight.setPaintTicks(true);
        ranking.add(new JLabel("A weight (%)"));
        ranking.add(weight);
        ranking.add(
                button(
                        "Mix A + B",
                        () -> controller.mix(a.getText(), b.getText(), weight.getValue() / 100.0)));
        ranking.add(button("Analyze A", () -> controller.analyze(a.getText())));
        controls.add(ranking);
        JPanel groupRow = row();
        groupRow.add(new JLabel("Group preference IDs (comma-separated)"));
        groupRow.add(group);
        groupRow.add(
                button(
                        "Recommend for group",
                        () ->
                                controller.group(
                                        Arrays.stream(group.getText().split(","))
                                                .map(String::trim)
                                                .filter(s -> !s.isEmpty())
                                                .distinct()
                                                .collect(Collectors.toList()))));
        controls.add(groupRow);
        add(controls, BorderLayout.NORTH);
        JPanel content = new JPanel(new BorderLayout());
        content.add(grid, BorderLayout.NORTH);
        add(new JScrollPane(content), BorderLayout.CENTER);
        status.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        add(status, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(1150, 780));
    }

    public void setController(DiscoveryController controller) {
        this.controller = controller;
    }

    @Override
    public void busy(boolean value) {
        enable(controls, !value);
        enable(grid, !value);
    }

    @Override
    public void status(String message) {
        status.setText(message);
        status.setToolTipText(message);
    }

    @Override
    public void show(List<Card> cards, String message) {
        grid.removeAll();
        for (Card card : cards) {
            grid.add(card(card));
        }
        grid.revalidate();
        grid.repaint();
        status(message);
    }

    private JPanel card(Card card) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createEtchedBorder(),
                        BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        JLabel image = new JLabel("Preview unavailable", SwingConstants.CENTER);
        if (card.thumbnail != null) {
            double scale =
                    Math.min(290.0 / card.thumbnail.getWidth(), 180.0 / card.thumbnail.getHeight());
            image.setText("");
            image.setIcon(
                    new ImageIcon(
                            card.thumbnail.getScaledInstance(
                                    Math.max(1, (int) (card.thumbnail.getWidth() * scale)),
                                    Math.max(1, (int) (card.thumbnail.getHeight() * scale)),
                                    Image.SCALE_SMOOTH)));
        }
        panel.add(image);
        JTextArea description = new JTextArea(card.photo.description(), 2, 24);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setEditable(false);
        description.setOpaque(false);
        panel.add(description);
        panel.add(new JLabel(card.score));
        JPanel links = row();
        links.add(
                button(
                        "Photo by " + card.photo.photographer(),
                        () -> open(card.photo.photographerUrl())));
        links.add(button("on Unsplash", () -> open(card.photo.pageUrl())));
        panel.add(links);
        JPanel likes = row();
        likes.add(button("Like → A", () -> controller.like(a.getText(), card.photo)));
        likes.add(button("Like → B", () -> controller.like(b.getText(), card.photo)));
        panel.add(likes);
        JPanel removal = row();
        removal.add(button("Remove from A", () -> controller.remove(a.getText(), card.photo)));
        removal.add(button("Remove from B", () -> controller.remove(b.getText(), card.photo)));
        panel.add(removal);
        return panel;
    }

    private void open(String url) {
        try {
            URI uri = URI.create(url);
            if (!"https".equals(uri.getScheme()) || !"unsplash.com".equals(uri.getHost())) {
                throw new IllegalArgumentException("Invalid source link");
            }
            if (!Desktop.isDesktopSupported()) {
                throw new IllegalStateException("Browser unavailable");
            }
            Desktop.getDesktop().browse(uri);
        } catch (Exception ex) {
            status("Open this source link in your browser: " + url);
        }
    }

    private static JPanel row() {
        return new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 3));
    }

    private static JButton button(String text, Runnable action) {
        JButton button = new JButton(text);
        button.addActionListener(event -> action.run());
        return button;
    }

    private static void enable(Component component, boolean enabled) {
        component.setEnabled(enabled);
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                enable(child, enabled);
            }
        }
    }
}
