package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Map;
import java.util.function.Supplier;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/** Navigation only; feature controllers and presenters own all use-case interaction. */
public final class MainView extends JPanel {
    private final JTabbedPane tabs = new JTabbedPane();
    public MainView(Map<String, Supplier<JPanel>> features) {
        super(new BorderLayout());
        features.forEach((name, factory) -> tabs.addTab(name, new LazyPanel(factory)));
        tabs.addChangeListener(event -> activateSelected(tabs));
        add(tabs, BorderLayout.CENTER);
        setPreferredSize(new Dimension(1100, 750));
        activateSelected(tabs);
    }

    //method for tab navigation
    public void selectTab(String name){
        int index = tabs.indexOfTab(name);
        if(index < 0){
            throw new IllegalArgumentException("unknown tab: "+name);
        }
        tabs.setSelectedIndex(index);
    }

    private static void activateSelected(JTabbedPane tabs) {
        if (tabs.getSelectedComponent() instanceof LazyPanel selected) {
            selected.activate();
        }
    }
}
