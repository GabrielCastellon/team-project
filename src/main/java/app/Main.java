package app;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/** Entry point: all Swing construction happens on the event dispatch thread. */
public final class Main {
    private Main() { }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Image Discovery — Team Project");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new ApplicationFactory().createView());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
