package App;

import App.application.LoginUseCase;
import App.application.RecoverAccountUseCase;
import App.application.RegisterUseCase;
import App.domain.AuthRepository;
import App.infrastructure.InMemoryAuthRepository;
import App.presentation.AuthFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName()
                );
            } catch (Exception e) {
                System.err.println(
                        "Could not load system appearance: " + e.getMessage()
                );
            }

            // Composition root: wire the application's layers together.
            AuthRepository repository = new InMemoryAuthRepository();

            LoginUseCase loginUseCase = new LoginUseCase(repository);
            RegisterUseCase registerUseCase = new RegisterUseCase(repository);
            RecoverAccountUseCase recoverAccountUseCase =
                    new RecoverAccountUseCase(repository);

            AuthFrame frame = new AuthFrame(
                    loginUseCase,
                    registerUseCase,
                    recoverAccountUseCase
            );
            frame.setVisible(true);
        });
    }
}
