
package App;

import view.MainView;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Application entry point.
 * Launches Photofy using the ApplicationFactory.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName()
                );
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            launchApplication();
        });
    }

    private static void launchApplication() {

        // Create the application and its shared dependencies.
        ApplicationFactory applicationFactory =
                new ApplicationFactory();

        // Build the main application interface.
        MainView mainView = applicationFactory.createView();

        // Create the application window.
        JFrame frame = new JFrame("Photofy");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(mainView);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
