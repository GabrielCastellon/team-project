package view;

import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** Explicit placeholder; this does not implement the owner's user story. */
public final class PlaceholderView extends JPanel {
    public PlaceholderView(String feature, String owner) {
        super(new BorderLayout());
        add(new JLabel(feature + " — implementation pending (" + owner + ")",
                SwingConstants.CENTER), BorderLayout.CENTER);
    }
}
