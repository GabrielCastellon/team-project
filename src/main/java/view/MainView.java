package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Map;
import java.util.function.Supplier;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/** Navigation only; feature controllers and presenters own all use-case interaction. */
public final class MainView extends JPanel {
    public MainView(Map<String, Supplier<JPanel>> features) {
        super(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();
        features.forEach((name, factory) -> tabs.addTab(name, new LazyPanel(factory)));
        tabs.addChangeListener(event -> activateSelected(tabs));
        add(tabs, BorderLayout.CENTER);
        setPreferredSize(new Dimension(1100, 750));
        activateSelected(tabs);
    }

    private static void activateSelected(JTabbedPane tabs) {
        if (tabs.getSelectedComponent() instanceof LazyPanel selected) {
            selected.activate();
        }
    }
}
