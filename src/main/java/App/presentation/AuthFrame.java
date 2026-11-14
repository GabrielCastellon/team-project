package App.presentation;

import App.application.LoginUseCase;
import App.application.RecoverAccountUseCase;
import App.application.RegisterUseCase;
import App.domain.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AuthFrame extends JFrame {
    private final LoginUseCase loginUseCase;
    private final RegisterUseCase registerUseCase;
    private final RecoverAccountUseCase recoverAccountUseCase;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel pages = new JPanel(cardLayout);

    private JTextField loginEmail;
    private JPasswordField loginPassword;

    private JTextField registerUsername;
    private JTextField registerEmail;
    private JPasswordField registerPassword;
    private JPasswordField confirmPassword;

    private JTextField recoveryEmail;

    public AuthFrame(
            LoginUseCase loginUseCase,
            RegisterUseCase registerUseCase,
            RecoverAccountUseCase recoverAccountUseCase
    ) {
        this.loginUseCase = loginUseCase;
        this.registerUseCase = registerUseCase;
        this.recoverAccountUseCase = recoverAccountUseCase;

        setTitle("Photofy | Account");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(760, 780);
        setMinimumSize(new Dimension(650, 650));
        setLocationRelativeTo(null);
        getContentPane().setBackground(PhotofyTheme.BACKGROUND);

        pages.add(createLoginPanel(), "login");
        pages.add(createRegisterPanel(), "register");
        pages.add(createRecoveryPanel(), "recovery");

        add(pages);
        showPage("login");
    }

    private void showPage(String page) {
        cardLayout.show(pages, page);
    }

    private JLabel heading(String text, int size) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, size));
        label.setForeground(PhotofyTheme.TEXT);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                label.getPreferredSize().height));
        return label;
    }

    private JLabel brand() {
        JLabel label = heading("- Photofy", 25);
        label.setForeground(PhotofyTheme.RED);
        return label;
    }

    private JLabel subtitle(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(PhotofyTheme.BODY);
        label.setForeground(Color.GRAY);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                label.getPreferredSize().height));
        return label;
    }

    private JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PhotofyTheme.CARD);
        panel.setBorder(new EmptyBorder(35, 45, 35, 45));

        // Keep the card at a predictable width.
        Dimension cardSize = new Dimension(660, 650);
        panel.setPreferredSize(cardSize);
        panel.setMinimumSize(cardSize);
        panel.setMaximumSize(new Dimension(660, Integer.MAX_VALUE));

        return panel;
    }

    private void addField(JPanel panel, String labelText, JComponent field) {

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(PhotofyTheme.TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Group each label and input so they stay together and centered.
        Dimension fieldSize = new Dimension(560, 52);

        field.setPreferredSize(fieldSize);
        field.setMinimumSize(fieldSize);
        field.setMaximumSize(fieldSize);
        field.setFont(new Font("SansSerif", Font.PLAIN, 17));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel fieldGroup = new JPanel();
        fieldGroup.setLayout(new BoxLayout(fieldGroup, BoxLayout.Y_AXIS));
        fieldGroup.setOpaque(false);
        Dimension groupSize = new Dimension(560, 82);
        fieldGroup.setPreferredSize(groupSize);
        fieldGroup.setMinimumSize(groupSize);
        fieldGroup.setMaximumSize(groupSize);
        fieldGroup.setAlignmentX(Component.CENTER_ALIGNMENT);

        fieldGroup.add(label);
        fieldGroup.add(Box.createVerticalStrut(8));
        fieldGroup.add(field);

        panel.add(fieldGroup);
        panel.add(Box.createVerticalStrut(20));
    }

    private JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(PhotofyTheme.RED);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(PhotofyTheme.BUTTON);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        Dimension buttonSize = new Dimension(560, 48);
        button.setMinimumSize(buttonSize);
        button.setMaximumSize(buttonSize);
        button.setPreferredSize(buttonSize);
        return button;
    }

    private JButton linkButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(PhotofyTheme.RED);
        button.setBackground(PhotofyTheme.CARD);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }

    private JPanel centeredPage(JPanel card) {
        JPanel outer = new JPanel(new GridBagLayout());

        outer.setBackground(PhotofyTheme.BACKGROUND);
        outer.setBorder(new EmptyBorder(20, 20, 20, 20));

        outer.add(card);

        return outer;
    }

    private JPanel createLoginPanel() {
        JPanel card = createCardPanel();

        card.add(brand());
        card.add(Box.createVerticalStrut(25));
        card.add(heading("Welcome Back!", 25));
        card.add(Box.createVerticalStrut(8));
        card.add(subtitle("Log in to continue to your account"));
        card.add(Box.createVerticalStrut(28));

        loginEmail = new JTextField();
        loginPassword = new JPasswordField();

        addField(card, "Email address", loginEmail);
        addField(card, "Password", loginPassword);

        JButton forgot = linkButton("Forgot your password?");
        forgot.addActionListener(e -> showPage("recovery"));
        card.add(forgot);
        card.add(Box.createVerticalStrut(14));

        JButton login = primaryButton("Log in");
        login.addActionListener(e -> handleLogin());
        card.add(login);

        card.add(Box.createVerticalStrut(18));
        card.add(subtitle("New to Photofy?"));

        JButton register = linkButton("Create an account");
        register.addActionListener(e -> showPage("register"));
        card.add(register);

        card.add(Box.createVerticalGlue());
        card.add(subtitle("Demo: demo@example.com / Pass1234"));

        return centeredPage(card);
    }

    private void handleLogin() {
        String email = loginEmail.getText().trim();
        String password = new String(loginPassword.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter your email and password.",
                    "Missing information");
            return;
        }

        User user = loginUseCase.execute(email, password).orElse(null);

        if (user == null) {
            showError("Invalid email or password.", "Login failed");
            return;
        }

        showWelcome(user);
    }

    private JPanel createRegisterPanel() {
        JPanel card = createCardPanel();

        card.add(brand());
        card.add(Box.createVerticalStrut(18));
        card.add(heading("Create an account", 24));
        card.add(Box.createVerticalStrut(8));
        card.add(subtitle("Join Photofy and share your ideas."));
        card.add(Box.createVerticalStrut(22));

        registerUsername = new JTextField();
        registerEmail = new JTextField();
        registerPassword = new JPasswordField();
        confirmPassword = new JPasswordField();

        addField(card, "Username (minimum 3 characters)", registerUsername);
        addField(card, "Email address", registerEmail);
        addField(card, "Password (minimum 8 characters)", registerPassword);
        addField(card, "Confirm password", confirmPassword);

        JButton register = primaryButton("Create Account");
        register.addActionListener(e -> handleRegister());
        card.add(register);
        card.add(Box.createVerticalStrut(14));

        JButton back = linkButton("Already registered? Log in");
        back.addActionListener(e -> showPage("login"));
        card.add(back);

        JScrollPane scrollPane = new JScrollPane(centeredPage(card));
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(PhotofyTheme.BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel container = new JPanel(new BorderLayout());
        container.add(scrollPane, BorderLayout.CENTER);
        return container;
    }

    private void handleRegister() {
        String username = registerUsername.getText().trim();
        String email = registerEmail.getText().trim();
        String password = new String(registerPassword.getPassword());
        String confirmation = new String(confirmPassword.getPassword());

        RegisterUseCase.Result result = registerUseCase.execute(
                username, email, password, confirmation
        );

        switch (result) {
            case INVALID_USERNAME:
                showError("Username must contain at least 3 characters.",
                        "Invalid username");
                return;
            case INVALID_EMAIL:
                showError("Please enter a valid email address.",
                        "Invalid email");
                return;
            case INVALID_PASSWORD:
                if (password.length() < 8) {
                    showError("Password must contain at least 8 characters.",
                            "Invalid password");
                } else {
                    showError("Passwords do not match.",
                            "Password mismatch");
                }
                return;
            case EMAIL_ALREADY_EXISTS:
                showError("An account with this email already exists.",
                        "Account already exists");
                return;
            case SUCCESS:
                JOptionPane.showMessageDialog(
                        this,
                        "Your account has been created! You can now log in.",
                        "Registration successful",
                        JOptionPane.INFORMATION_MESSAGE
                );
                loginEmail.setText(email);
                registerUsername.setText("");
                registerEmail.setText("");
                registerPassword.setText("");
                confirmPassword.setText("");
                showPage("login");
        }
    }

    private JPanel createRecoveryPanel() {
        JPanel card = createCardPanel();

        card.add(brand());
        card.add(Box.createVerticalStrut(30));
        card.add(heading("Account recovery", 24));
        card.add(Box.createVerticalStrut(12));
        card.add(subtitle("Enter the email for your account."));
        card.add(Box.createVerticalStrut(25));

        recoveryEmail = new JTextField();
        addField(card, "Email address", recoveryEmail);

        JButton submit = primaryButton("Continue");
        submit.addActionListener(e -> handleRecovery());
        card.add(submit);
        card.add(Box.createVerticalStrut(15));

        JButton back = linkButton("Back to login");
        back.addActionListener(e -> showPage("login"));
        card.add(back);

        return centeredPage(card);
    }

    private void handleRecovery() {
        String email = recoveryEmail.getText().trim();

        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            showError("Please enter a valid email address.",
                    "Invalid email");
            return;
        }

        // Deliberately use a generic response to avoid revealing whether
        // an email is registered. No email is sent in this prototype.
        recoverAccountUseCase.execute(email);

        JOptionPane.showMessageDialog(
                this,
                "If an account is associated with that address, " +
                        "you would receive password-reset instructions. " +
                        "Email delivery is not implemented in this demo.",
                "Recovery request",
                JOptionPane.INFORMATION_MESSAGE
        );

        recoveryEmail.setText("");
        showPage("login");
    }

    private void showWelcome(User user) {
        JPanel card = createCardPanel();

        card.add(brand());
        card.add(Box.createVerticalStrut(40));
        card.add(heading("Welcome, " + user.getUsername() + "!", 24));
        card.add(Box.createVerticalStrut(12));
        card.add(subtitle("You have successfully logged in."));
        card.add(Box.createVerticalStrut(35));

        JButton logout = primaryButton("Log out");
        logout.addActionListener(e -> {
            loginPassword.setText("");
            showPage("login");
        });
        card.add(logout);

        // Replace the existing welcome page instead of adding duplicates.
        for (Component component : pages.getComponents()) {
            if ("welcome".equals(component.getName())) {
                pages.remove(component);
                break;
            }
        }

        JPanel welcomePage = centeredPage(card);
        welcomePage.setName("welcome");
        pages.add(welcomePage, "welcome");
        pages.revalidate();
        pages.repaint();
        showPage("welcome");
    }

    private void showError(String message, String title) {
        JOptionPane.showMessageDialog(
                this,
                message,
                title,
                JOptionPane.WARNING_MESSAGE
        );
    }
}
