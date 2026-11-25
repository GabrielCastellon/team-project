package app.presentation;

import entity.SessionManager;
import entity.User;
import interface_adapter.login.LoginController;
import interface_adapter.login.LoginViewModel;
import interface_adapter.signup.SignupController;
import interface_adapter.signup.SignupViewModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Photofy account GUI.
 * Uses username/password only; it does not collect or depend on email addresses.
 */
public class AuthPanel extends JPanel {
    private final LoginController loginController;
    private final SignupController signupController;
    private final LoginViewModel loginViewModel;
    private final SignupViewModel signupViewModel;
    private final SessionManager sessionManager;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel pages = new JPanel(cardLayout);

    private JTextField loginUsername;
    private JPasswordField loginPassword;
    private JTextField registerUsername;
    private JPasswordField registerPassword;
    private JPasswordField confirmPassword;

    public AuthPanel(LoginController loginController,
                     SignupController signupController,
                     LoginViewModel loginViewModel,
                     SignupViewModel signupViewModel,
                     SessionManager sessionManager) {
        this.loginController = loginController;
        this.signupController = signupController;
        this.loginViewModel = loginViewModel;
        this.signupViewModel = signupViewModel;
        this.sessionManager = sessionManager;

        setBackground(PhotofyTheme.BACKGROUND);
        setLayout(new BorderLayout());

        pages.add(createLoginPanel(), "login");
        pages.add(createRegisterPanel(), "register");

        add(pages, BorderLayout.CENTER);

        showPage("login");
    }

    private void showPage(String page) {
        cardLayout.show(pages, page);
    }

    private JLabel heading(String text, int size) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, size));
        label.setForeground(PhotofyTheme.TEXT);
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
        Dimension size = new Dimension(660, 650);
        panel.setPreferredSize(size);
        panel.setMinimumSize(size);
        panel.setMaximumSize(new Dimension(660, Integer.MAX_VALUE));
        return panel;
    }

    private void addField(JPanel panel, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(PhotofyTheme.TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        Dimension fieldSize = new Dimension(560, 52);
        field.setPreferredSize(fieldSize);
        field.setMinimumSize(fieldSize);
        field.setMaximumSize(fieldSize);
        field.setFont(new Font("SansSerif", Font.PLAIN, 17));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setOpaque(false);
        Dimension groupSize = new Dimension(560, 82);
        group.setPreferredSize(groupSize);
        group.setMinimumSize(groupSize);
        group.setMaximumSize(groupSize);
        group.setAlignmentX(Component.CENTER_ALIGNMENT);
        group.add(label);
        group.add(Box.createVerticalStrut(8));
        group.add(field);
        panel.add(group);
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
        Dimension size = new Dimension(560, 48);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
        button.setPreferredSize(size);
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

        loginUsername = new JTextField();
        loginPassword = new JPasswordField();
        addField(card, "Username", loginUsername);
        addField(card, "Password", loginPassword);

        JButton forgot = linkButton("Forgot your password?");
        forgot.addActionListener(e -> showRecoveryNotice());
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
        card.add(subtitle("Log in using your username and password."));
        return centeredPage(card);
    }

    private void handleLogin() {
        String username = loginUsername.getText().trim();
        String password = new String(loginPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter your username and password.",
                    "Missing information");
            return;
        }

        loginController.execute(username, password);
        if (!loginViewModel.getState().isSuccess()) {
            showError(loginViewModel.getState().getErrorMessage() == null
                    ? "Invalid username or password."
                    : loginViewModel.getState().getErrorMessage(), "Login failed");
            return;
        }

        showWelcome(loginViewModel.getState().getUser());
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
        registerPassword = new JPasswordField();
        confirmPassword = new JPasswordField();
        addField(card, "Username", registerUsername);
        addField(card, "Password", registerPassword);
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
        String password = new String(registerPassword.getPassword());
        String confirmation = new String(confirmPassword.getPassword());

        if (username.isEmpty()) {
            showError("Please enter a username.", "Invalid username");
            return;
        }
        if (password.length() < 8) {
            showError("Password must contain at least 8 characters.",
                    "Invalid password");
            return;
        }
        if (!password.equals(confirmation)) {
            showError("Passwords do not match.", "Password mismatch");
            return;
        }

        signupController.execute(username, password, confirmation);
        if (!signupViewModel.getState().isSuccess()) {
            showError(signupViewModel.getState().getErrorMessage() == null
                    ? "Unable to create account."
                    : signupViewModel.getState().getErrorMessage(),
                    "Registration failed");
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Your account has been created! You can now log in.",
                "Registration successful", JOptionPane.INFORMATION_MESSAGE);
        loginUsername.setText(username);
        registerUsername.setText("");
        registerPassword.setText("");
        confirmPassword.setText("");
        showPage("login");
    }

    private void showRecoveryNotice() {
        JOptionPane.showMessageDialog(this,
                "Account recovery is not connected yet. The current account "
                        + "data model stores a username and password hash only. "
                        + "A security-question recovery feature needs its own "
                        + "approved storage design before it can reset passwords.",
                "Account recovery", JOptionPane.INFORMATION_MESSAGE);
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
            sessionManager.logout();
            loginPassword.setText("");
            showPage("login");
        });
        card.add(logout);

        Component oldWelcome = null;
        for (Component component : pages.getComponents()) {
            if ("welcome".equals(component.getName())) {
                oldWelcome = component;
                break;
            }
        }
        if (oldWelcome != null) pages.remove(oldWelcome);

        JPanel welcomePage = centeredPage(card);
        welcomePage.setName("welcome");
        pages.add(welcomePage, "welcome");
        pages.revalidate();
        pages.repaint();
        showPage("welcome");
    }

    private void showError(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title,
                JOptionPane.WARNING_MESSAGE);
    }
}
