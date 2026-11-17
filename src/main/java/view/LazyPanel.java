package view;

import java.awt.BorderLayout;
import java.util.Objects;
import java.util.function.Supplier;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/** Virtual Proxy for a feature panel: construct its real content only when selected. */
public final class LazyPanel extends JPanel {
    private final Supplier<JPanel> factory;
    private JPanel content;

    public LazyPanel(Supplier<JPanel> factory) {
        super(new BorderLayout());
        this.factory = Objects.requireNonNull(factory);
    }

    /** Activates once on success; failed initialization is isolated and can be retried. */
    public void activate() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Activate feature panels on the Swing EDT");
        }
        if (content != null) {
            return;
        }
        removeAll();
        try {
            content = Objects.requireNonNull(factory.get(), "Feature factory returned null");
            add(content, BorderLayout.CENTER);
        } catch (RuntimeException exception) {
            String detail = exception.getMessage();
            add(new JLabel("Could not open feature: "
                    + (detail == null ? exception.getClass().getSimpleName() : detail)),
                    BorderLayout.CENTER);
            JButton retry = new JButton("Retry");
            retry.addActionListener(event -> activate());
            add(retry, BorderLayout.SOUTH);
        }
        revalidate();
        repaint();
    }
}
