package view;

import interface_adapter.profile.EditProfileController;
import interface_adapter.profile.ProfileState;
import interface_adapter.profile.ProfileViewModel;
import interface_adapter.profile.ProfileViewModelListener;
import interface_adapter.profile.ViewProfileController;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * The Profile tab. Display mode shows the stored profile; edit mode is a form whose
 * Save goes to {@link EditProfileController}. The view only talks to its controllers
 * and reads {@link ProfileViewModel}; it never validates or touches the disk itself.
 */
public final class ProfileView extends JPanel implements ProfileViewModelListener {
    private static final String DISPLAY_CARD = "display";
    private static final String EDIT_CARD = "edit";
    private static final Color ERROR_COLOR = new Color(176, 0, 32);

    private final ProfileViewModel viewModel;
    private final EditProfileController editProfileController;

    private final CardLayout cards = new CardLayout();
    private final JPanel cardPanel = new JPanel(cards);
    private final JLabel statusLabel = new JLabel(" ");

    // display mode
    private final JLabel nameLabel = new JLabel();
    private final JTextArea bioText = new JTextArea(4, 30);
    private final JLabel avatarLabel = new JLabel();

    // edit mode
    private final JTextField nameField = new JTextField(30);
    private final JTextArea bioField = new JTextArea(5, 30);
    private final JTextField avatarField = new JTextField(30);
    private final JLabel nameError = errorLabel();
    private final JLabel bioError = errorLabel();
    private final JLabel avatarError = errorLabel();
    private final JButton saveButton = new JButton("Save");

    public ProfileView(ViewProfileController viewProfileController,
                       EditProfileController editProfileController,
                       ProfileViewModel viewModel) {
        super(new BorderLayout());
        this.viewModel = viewModel;
        this.editProfileController = editProfileController;

        cardPanel.add(buildDisplayCard(), DISPLAY_CARD);
        cardPanel.add(buildEditCard(), EDIT_CARD);
        add(cardPanel, BorderLayout.CENTER);
        statusLabel.setName("statusLabel");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        add(statusLabel, BorderLayout.SOUTH);

        // register before loading so the first result can't be missed
        viewModel.addListener(this);
        statusLabel.setText("Loading profile…");
        viewProfileController.execute();
    }

    // Called on the executor's background thread: read the state once, then
    // touch Swing components only on the event dispatch thread.
    @Override
    public void stateChanged() {
        ProfileState state = viewModel.getState();
        SwingUtilities.invokeLater(() -> render(state));
    }

    private void render(ProfileState state) {
        nameLabel.setText(state.getDisplayName());
        bioText.setText(state.getBiography());
        avatarLabel.setText(avatarName(state.getAvatarPath()));
        nameError.setText(state.getNameError());
        bioError.setText(state.getBioError());
        avatarError.setText(state.getAvatarError());
        statusLabel.setText(state.getStatusMessage().isEmpty() ? " " : state.getStatusMessage());
        saveButton.setEnabled(true);
        // only a successful save leaves edit mode; on failure the form keeps the user's edits
        if (state.isSaved()) {
            cards.show(cardPanel, DISPLAY_CARD);
        }
    }

    private JPanel buildDisplayCard() {
        nameLabel.setName("displayNameLabel");
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 20f));
        bioText.setEditable(false);
        bioText.setLineWrap(true);
        bioText.setWrapStyleWord(true);
        bioText.setOpaque(false);
        avatarLabel.setName("avatarLabel");

        JButton editButton = new JButton("Edit Profile");
        editButton.addActionListener(event -> startEditing());

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = constraints();
        addRow(panel, c, new JLabel("Name"), nameLabel);
        addRow(panel, c, new JLabel("Bio"), bioText);
        addRow(panel, c, new JLabel("Avatar"), avatarLabel);
        c.gridx = 1;
        c.fill = GridBagConstraints.NONE;
        panel.add(editButton, c);
        return wrapTop(panel);
    }

    private JPanel buildEditCard() {
        nameField.setName("nameField");
        bioField.setLineWrap(true);
        bioField.setWrapStyleWord(true);
        avatarField.setName("avatarField");

        JButton browseButton = new JButton("Browse…");
        browseButton.addActionListener(event -> chooseAvatar());
        JPanel avatarRow = new JPanel(new BorderLayout(4, 0));
        avatarRow.add(avatarField, BorderLayout.CENTER);
        avatarRow.add(browseButton, BorderLayout.EAST);

        saveButton.addActionListener(event -> save());
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(event -> cancelEditing());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttons.add(saveButton);
        buttons.add(cancelButton);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = constraints();
        addRow(panel, c, new JLabel("Display name"), nameField);
        addRow(panel, c, new JLabel(), nameError);
        addRow(panel, c, new JLabel("Bio"), new JScrollPane(bioField));
        addRow(panel, c, new JLabel(), bioError);
        addRow(panel, c, new JLabel("Avatar"), avatarRow);
        addRow(panel, c, new JLabel(), avatarError);
        c.gridx = 1;
        c.fill = GridBagConstraints.NONE;
        panel.add(buttons, c);
        return wrapTop(panel);
    }

    // Main flow step 3: the form starts from the stored values.
    private void startEditing() {
        ProfileState state = viewModel.getState();
        nameField.setText(state.getDisplayName());
        bioField.setText(state.getBiography());
        avatarField.setText(state.getAvatarPath());
        clearMessages();
        cards.show(cardPanel, EDIT_CARD);
    }

    // BR4a: discard edits; nothing is sent to the controller, the stored values are untouched.
    private void cancelEditing() {
        ProfileState state = viewModel.getState();
        nameField.setText(state.getDisplayName());
        bioField.setText(state.getBiography());
        avatarField.setText(state.getAvatarPath());
        clearMessages();
        cards.show(cardPanel, DISPLAY_CARD);
    }

    private void save() {
        // disabled until the result arrives, so a double click can't submit twice
        saveButton.setEnabled(false);
        statusLabel.setText("Saving…");
        editProfileController.execute(nameField.getText(), bioField.getText(), avatarField.getText().trim());
    }

    private void chooseAvatar() {
        JFileChooser chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("Images (png, jpg, jpeg)", "png", "jpg", "jpeg"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            avatarField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void clearMessages() {
        nameError.setText("");
        bioError.setText("");
        avatarError.setText("");
        statusLabel.setText(" ");
    }

    // display mode shows just the file name, not the whole path
    private static String avatarName(String avatarPath) {
        return avatarPath.isEmpty() ? "No avatar" : new File(avatarPath).getName();
    }

    private static JLabel errorLabel() {
        JLabel label = new JLabel();
        label.setForeground(ERROR_COLOR);
        return label;
    }

    private static GridBagConstraints constraints() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.NORTHWEST;
        return c;
    }

    private static void addRow(JPanel panel, GridBagConstraints c, JLabel label, Component field) {
        c.gridx = 0;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        panel.add(label, c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, c);
        c.gridy++;
    }

    // keeps the form at the top-left instead of floating in the middle of a large tab
    private static JPanel wrapTop(JPanel content) {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        outer.add(content, BorderLayout.NORTH);
        return outer;
    }
}
