package view;

import interface_adapter.profile.AvatarImageLoader;
import interface_adapter.profile.EditProfileController;
import interface_adapter.profile.ProfileState;
import interface_adapter.profile.ProfileViewModel;
import interface_adapter.profile.ProfileViewModelListener;
import interface_adapter.profile.ViewProfileController;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * The Profile tab. Display mode shows the stored profile; edit mode is a form whose
 * Save goes to {@link EditProfileController}. The view only talks to its controllers
 * and reads {@link ProfileViewModel}; it never validates or touches the disk itself.
 * Styled with the team's {@link PhotofyTheme} so it matches the Accounts and Boards tabs.
 */
public final class ProfileView extends JPanel implements ProfileViewModelListener {
    /** Diameter, in pixels, of the round profile picture in display mode. */
    public static final int AVATAR_SIZE = 128;
    private static final int PREVIEW_SIZE = 72;
    private static final int FORM_WIDTH = 440;

    private static final String DISPLAY_CARD = "display";
    private static final String EDIT_CARD = "edit";
    private static final String SIGN_IN_CARD = "signIn";
    // which avatar path a picture label is showing or loading, so late results can be ignored
    private static final String AVATAR_PATH_KEY = "avatarPath";

    // colours PhotofyTheme doesn't have; kept here so the shared theme file stays as its owner wrote it
    private static final Color MUTED = new Color(118, 118, 118);
    private static final Color BIO_TEXT = new Color(80, 80, 80);
    private static final Color FIELD_BORDER = new Color(218, 214, 208);
    private static final Color CARD_BORDER = new Color(230, 226, 220);
    private static final Color PLACEHOLDER = new Color(229, 225, 219);

    private final ProfileViewModel viewModel;
    private final ViewProfileController viewProfileController;
    private final EditProfileController editProfileController;
    private final AvatarImageLoader avatarLoader;
    // reads pictures off the EDT and hands the result back on the EDT (Kevin's runner, as Pins uses)
    private final SwingTaskRunner<ImageIcon> avatarTasks = new SwingTaskRunner<>();

    private final CardLayout cards = new CardLayout();
    private final JPanel cardPanel = new JPanel(cards);

    // signed-out mode: only a message, no profile and no Edit button
    private final JLabel signInLabel = new JLabel("", SwingConstants.CENTER);

    // display mode
    private final JLabel avatarLabel = new JLabel();
    private final JLabel avatarCaption = new JLabel(" ");
    private final JLabel nameLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel bioLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel displayStatus = new JLabel(" ", SwingConstants.CENTER);
    private final JButton editButton = primaryButton("Edit profile");

    // edit mode
    private final JTextField nameField = new JTextField();
    private final JTextArea bioField = new JTextArea(4, 0);
    // holds the chosen photo's path; set by Choose photo / Remove photo rather than typed, so it isn't shown
    private final JTextField avatarField = new JTextField();
    private final JLabel avatarPreview = new JLabel();
    private final JLabel nameError = errorLabel();
    private final JLabel bioError = errorLabel();
    private final JLabel avatarError = errorLabel();
    private final JLabel editStatus = new JLabel(" ");
    private final JButton saveButton = primaryButton("Save");
    private final JButton cancelButton = secondaryButton("Cancel");
    private final JButton browseButton = secondaryButton("Choose photo…");
    private final JButton removePhotoButton = linkButton("Remove photo");

    public ProfileView(ViewProfileController viewProfileController,
                       EditProfileController editProfileController,
                       ProfileViewModel viewModel,
                       AvatarImageLoader avatarLoader) {
        super(new BorderLayout());
        this.viewModel = viewModel;
        this.viewProfileController = viewProfileController;
        this.editProfileController = editProfileController;
        this.avatarLoader = avatarLoader;

        setBackground(PhotofyTheme.BACKGROUND);
        cardPanel.setOpaque(false);
        cardPanel.add(buildDisplayCard(), DISPLAY_CARD);
        cardPanel.add(buildEditCard(), EDIT_CARD);
        cardPanel.add(buildSignInCard(), SIGN_IN_CARD);
        add(cardPanel, BorderLayout.CENTER);

        // register before loading so the first result can't be missed
        viewModel.addListener(this);
        reload();
    }

    /**
     * Loads the signed-in user's profile again. Called when the view is created and after every
     * sign-in, account switch or logout. Must run on the event dispatch thread.
     */
    public void reload() {
        // an open form belongs to whoever was signed in before, so it is discarded, never saved
        cards.show(cardPanel, DISPLAY_CARD);
        clearErrors();
        // hide the previous account's values while the new ones load
        nameLabel.setText("");
        bioLabel.setText("");
        avatarLabel.putClientProperty(AVATAR_PATH_KEY, null);
        avatarLabel.setIcon(placeholder(AVATAR_SIZE));
        avatarCaption.setText(" ");
        // nothing to edit until the stored profile has arrived
        editButton.setEnabled(false);
        setStatus("Loading profile…", false);
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
        bioLabel.setText(bioHtml(state.getBiography()));
        showDisplayAvatar(state);
        showError(nameError, state.getNameError());
        showError(bioError, state.getBioError());
        showError(avatarError, state.getAvatarError());
        // a result arrived, so any load or save in progress has finished
        setFormEnabled(true);
        editButton.setEnabled(true);
        if (!state.isSignedIn()) {
            signInLabel.setText(state.getStatusMessage());
            setStatus("", false);
            cards.show(cardPanel, SIGN_IN_CARD);
            return;
        }
        // "Profile updated" is the only status that isn't a problem
        setStatus(state.getStatusMessage(), !state.isSaved());
        if (state.isSaved()) {
            // only a successful save leaves edit mode; on failure the form keeps the user's edits
            cards.show(cardPanel, DISPLAY_CARD);
        }
    }

    // ---------------------------------------------------------------- building the cards

    private JPanel buildDisplayCard() {
        nameLabel.setName("displayNameLabel");
        nameLabel.setFont(PhotofyTheme.TITLE.deriveFont(30f));
        nameLabel.setForeground(PhotofyTheme.TEXT);
        bioLabel.setFont(PhotofyTheme.BODY.deriveFont(14f));
        bioLabel.setForeground(BIO_TEXT);
        avatarLabel.setName("avatarLabel");
        avatarCaption.setName("avatarCaption");
        styleMuted(avatarCaption);
        displayStatus.setName("statusLabel");
        styleMuted(displayStatus);
        editButton.addActionListener(event -> startEditing());

        JPanel column = verticalPanel();
        column.setBorder(BorderFactory.createEmptyBorder(56, 24, 24, 24));
        addCentered(column, avatarLabel);
        column.add(Box.createVerticalStrut(4));
        addCentered(column, avatarCaption);
        column.add(Box.createVerticalStrut(12));
        addCentered(column, nameLabel);
        column.add(Box.createVerticalStrut(8));
        addCentered(column, bioLabel);
        column.add(Box.createVerticalStrut(28));
        addCentered(column, editButton);
        column.add(Box.createVerticalStrut(14));
        addCentered(column, displayStatus);
        return topAligned(column);
    }

    private JPanel buildEditCard() {
        nameField.setName("nameField");
        avatarField.setName("avatarField");
        avatarField.setVisible(false);
        avatarPreview.setName("avatarPreview");
        styleField(nameField, nameField);
        bioField.setLineWrap(true);
        bioField.setWrapStyleWord(true);
        bioField.setFont(PhotofyTheme.FIELD);
        JScrollPane bioScroll = new JScrollPane(bioField);
        styleField(bioField, bioScroll);
        fixWidth(nameField, nameField.getPreferredSize().height);
        fixWidth(bioScroll, 112);
        editStatus.setFont(PhotofyTheme.BODY);

        browseButton.addActionListener(event -> chooseAvatar());
        removePhotoButton.addActionListener(event -> avatarField.setText(""));
        saveButton.addActionListener(event -> save());
        cancelButton.addActionListener(event -> cancelEditing());
        // the preview follows the chosen photo, and the red initial follows the name while there's no photo
        onTextChange(avatarField, this::updatePreview);
        onTextChange(nameField, this::updatePreview);

        JPanel photoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        photoRow.setOpaque(false);
        photoRow.add(avatarPreview);
        photoRow.add(Box.createHorizontalStrut(16));
        photoRow.add(browseButton);
        photoRow.add(Box.createHorizontalStrut(8));
        photoRow.add(removePhotoButton);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttons.setOpaque(false);
        buttons.add(saveButton);
        buttons.add(Box.createHorizontalStrut(10));
        buttons.add(cancelButton);

        JLabel heading = new JLabel("Edit profile");
        heading.setFont(PhotofyTheme.HEADING);
        heading.setForeground(PhotofyTheme.TEXT);

        JPanel form = verticalPanel();
        form.setOpaque(true);
        form.setBackground(PhotofyTheme.CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER),
                BorderFactory.createEmptyBorder(32, 40, 32, 40)));
        addLeft(form, heading);
        form.add(Box.createVerticalStrut(24));
        addLeft(form, photoRow);
        addLeft(form, avatarError);
        addLeft(form, avatarField);
        form.add(Box.createVerticalStrut(20));
        addLeft(form, fieldLabel("Display name"));
        form.add(Box.createVerticalStrut(6));
        addLeft(form, nameField);
        addLeft(form, nameError);
        form.add(Box.createVerticalStrut(16));
        addLeft(form, fieldLabel("Bio"));
        form.add(Box.createVerticalStrut(6));
        addLeft(form, bioScroll);
        addLeft(form, bioError);
        form.add(Box.createVerticalStrut(24));
        addLeft(form, buttons);
        form.add(Box.createVerticalStrut(12));
        addLeft(form, editStatus);

        // centre the white card horizontally, near the top of the tab
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setOpaque(false);
        outer.setBorder(BorderFactory.createEmptyBorder(32, 24, 24, 24));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.weighty = 1;
        outer.add(form, c);
        return outer;
    }

    private JPanel buildSignInCard() {
        signInLabel.setName("signInLabel");
        signInLabel.setFont(PhotofyTheme.HEADING);
        signInLabel.setForeground(PhotofyTheme.TEXT);
        JLabel hint = new JLabel("Use the Accounts tab to sign in or create an account.");
        styleMuted(hint);

        JPanel column = verticalPanel();
        column.setBorder(BorderFactory.createEmptyBorder(150, 24, 24, 24));
        addCentered(column, signInLabel);
        column.add(Box.createVerticalStrut(8));
        addCentered(column, hint);
        return topAligned(column);
    }

    // ---------------------------------------------------------------- editing

    // Main flow step 3: the form starts from the stored values.
    private void startEditing() {
        fillFormFromState();
        cards.show(cardPanel, EDIT_CARD);
        nameField.requestFocusInWindow();
    }

    // BR4a: discard edits; nothing is sent to the controller, the stored values are untouched.
    private void cancelEditing() {
        fillFormFromState();
        cards.show(cardPanel, DISPLAY_CARD);
    }

    private void fillFormFromState() {
        ProfileState state = viewModel.getState();
        nameField.setText(state.getDisplayName());
        bioField.setText(state.getBiography());
        avatarField.setText(state.getAvatarPath());
        clearErrors();
        setStatus("", false);
    }

    private void save() {
        // locked until the result arrives: no double submit, and no Cancel that the save would then overrule
        setFormEnabled(false);
        setStatus("Saving…", false);
        editProfileController.execute(nameField.getText(), bioField.getText(), avatarField.getText().trim());
    }

    private void setFormEnabled(boolean enabled) {
        saveButton.setEnabled(enabled);
        cancelButton.setEnabled(enabled);
        nameField.setEnabled(enabled);
        bioField.setEnabled(enabled);
        avatarField.setEnabled(enabled);
        browseButton.setEnabled(enabled);
        removePhotoButton.setEnabled(enabled);
    }

    private void chooseAvatar() {
        JFileChooser chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("Images (png, jpg, jpeg)", "png", "jpg", "jpeg"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            avatarField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void updatePreview() {
        String path = avatarField.getText().trim();
        removePhotoButton.setVisible(!path.isEmpty());
        if (path.isEmpty()) {
            avatarPreview.putClientProperty(AVATAR_PATH_KEY, "");
            avatarPreview.setIcon(monogram(nameField.getText(), PREVIEW_SIZE));
            describe(avatarPreview, false);
        } else if (!path.equals(avatarPreview.getClientProperty(AVATAR_PATH_KEY))) {
            loadAvatar(avatarPreview, path, nameField.getText(), PREVIEW_SIZE, () -> { });
        }
    }

    // ---------------------------------------------------------------- the profile picture

    private void showDisplayAvatar(ProfileState state) {
        String path = state.getAvatarPath();
        if (path.isEmpty()) {
            // no photo: the red initial, redrawn every time since the name may have changed
            avatarLabel.putClientProperty(AVATAR_PATH_KEY, "");
            avatarLabel.setIcon(monogram(state.getDisplayName(), AVATAR_SIZE));
            describe(avatarLabel, false);
            avatarCaption.setText(" ");
        } else if (!path.equals(avatarLabel.getClientProperty(AVATAR_PATH_KEY))) {
            // only load the photo when the path changed, not on every error or status update
            avatarCaption.setText(" ");
            loadAvatar(avatarLabel, path, state.getDisplayName(), AVATAR_SIZE,
                    () -> avatarCaption.setText("Picture can't be shown"));
        }
    }

    private void loadAvatar(JLabel label, String path, String name, int size, Runnable onFailure) {
        label.putClientProperty(AVATAR_PATH_KEY, path);
        label.setIcon(placeholder(size));
        describe(label, false);
        avatarTasks.run(() -> circular(avatarLoader.load(path), size), (icon, failure) -> {
            if (!path.equals(label.getClientProperty(AVATAR_PATH_KEY))) {
                return; // the photo changed while this one was loading, so it's stale
            }
            if (failure != null) {
                // e.g. the file was moved or deleted after it was saved
                label.setIcon(monogram(name, size));
                onFailure.run();
                return;
            }
            label.setIcon(icon);
            describe(label, true);
        });
    }

    // screen readers (and tests) can tell a real photo from the initial
    private static void describe(JLabel label, boolean hasPhoto) {
        label.getAccessibleContext().setAccessibleDescription(
                hasPhoto ? "Profile picture" : "No profile picture");
    }

    // centre-crop to a square, scale, then clip to a smooth circle
    private static ImageIcon circular(BufferedImage photo, int size) {
        int side = Math.min(photo.getWidth(), photo.getHeight());
        BufferedImage square = photo.getSubimage(
                (photo.getWidth() - side) / 2, (photo.getHeight() - side) / 2, side, side);
        Image scaled = new ImageIcon(square.getScaledInstance(size, size, Image.SCALE_SMOOTH)).getImage();
        BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = smooth(out);
        g.fill(new Ellipse2D.Double(0, 0, size, size));
        g.setComposite(AlphaComposite.SrcIn);
        g.drawImage(scaled, 0, 0, null);
        g.dispose();
        return new ImageIcon(out);
    }

    // no photo yet: a red circle with the first letter of the display name
    private static ImageIcon monogram(String name, int size) {
        BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = smooth(out);
        g.setColor(PhotofyTheme.RED);
        g.fill(new Ellipse2D.Double(0, 0, size, size));
        String trimmed = name == null ? "" : name.trim();
        if (!trimmed.isEmpty()) {
            String initial = trimmed.substring(0, trimmed.offsetByCodePoints(0, 1)).toUpperCase(Locale.ROOT);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, Math.round(size * 0.44f)));
            FontMetrics metrics = g.getFontMetrics();
            int x = (size - metrics.stringWidth(initial)) / 2;
            int y = (size - metrics.getHeight()) / 2 + metrics.getAscent();
            g.drawString(initial, x, y);
        }
        g.dispose();
        return new ImageIcon(out);
    }

    // shown while a photo loads
    private static ImageIcon placeholder(int size) {
        BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = smooth(out);
        g.setColor(PLACEHOLDER);
        g.fill(new Ellipse2D.Double(0, 0, size, size));
        g.dispose();
        return new ImageIcon(out);
    }

    private static Graphics2D smooth(BufferedImage image) {
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        return g;
    }

    // ---------------------------------------------------------------- messages

    private void setStatus(String message, boolean problem) {
        String text = message.isEmpty() ? " " : message;
        for (JLabel label : new JLabel[] {displayStatus, editStatus}) {
            label.setText(text);
            label.setForeground(problem ? PhotofyTheme.RED : MUTED);
        }
    }

    private void clearErrors() {
        showError(nameError, "");
        showError(bioError, "");
        showError(avatarError, "");
    }

    // hidden when empty so an unused error line doesn't leave a gap in the form
    private static void showError(JLabel label, String message) {
        label.setText(message);
        label.setVisible(!message.isEmpty());
    }

    // centred, wrapped bio; an empty bio gets a quiet prompt instead of a blank gap
    private static String bioHtml(String biography) {
        if (biography.isBlank()) {
            return "<html><div style='text-align:center;color:#767676'>No bio yet</div></html>";
        }
        String escaped = biography.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\n", "<br>");
        return "<html><div style='text-align:center;width:420px'>" + escaped + "</div></html>";
    }

    // ---------------------------------------------------------------- small styling helpers

    private static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(PhotofyTheme.RED);
        button.setForeground(Color.WHITE);
        button.setFont(PhotofyTheme.BUTTON);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(11, 26, 11, 26));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private static JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(PhotofyTheme.CARD);
        button.setForeground(PhotofyTheme.TEXT);
        button.setFont(PhotofyTheme.BUTTON);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private static JButton linkButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(PhotofyTheme.RED);
        button.setFont(PhotofyTheme.BODY);
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 6, 10, 6));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    // light border that turns red while the field has keyboard focus
    private static void styleField(JComponent focusSource, JComponent bordered) {
        focusSource.setFont(PhotofyTheme.FIELD);
        Border padding = BorderFactory.createEmptyBorder(8, 10, 8, 10);
        Border normal = BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER), BorderFactory.createEmptyBorder(1, 1, 1, 1)), padding);
        Border focused = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PhotofyTheme.RED, 2), padding);
        if (bordered instanceof JScrollPane scroll) {
            // the padding goes on the text area inside, the outline on the scroll pane around it
            focusSource.setBorder(padding);
            normal = BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(FIELD_BORDER), BorderFactory.createEmptyBorder(1, 1, 1, 1));
            focused = BorderFactory.createLineBorder(PhotofyTheme.RED, 2);
            scroll.setViewportBorder(null);
        }
        Border idle = normal;
        Border active = focused;
        bordered.setBorder(idle);
        focusSource.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                bordered.setBorder(active);
            }

            @Override
            public void focusLost(FocusEvent event) {
                bordered.setBorder(idle);
            }
        });
    }

    private static void fixWidth(JComponent component, int height) {
        Dimension size = new Dimension(FORM_WIDTH, height);
        component.setPreferredSize(size);
        component.setMaximumSize(size);
    }

    private static JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(PhotofyTheme.BODY.deriveFont(Font.BOLD));
        label.setForeground(PhotofyTheme.TEXT);
        return label;
    }

    private static JLabel errorLabel() {
        JLabel label = new JLabel();
        label.setFont(PhotofyTheme.BODY.deriveFont(12f));
        label.setForeground(PhotofyTheme.RED);
        label.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        label.setVisible(false);
        return label;
    }

    private static void styleMuted(JLabel label) {
        label.setFont(PhotofyTheme.BODY);
        label.setForeground(MUTED);
    }

    private static void onTextChange(JTextField field, Runnable action) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                action.run();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                action.run();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                action.run();
            }
        });
    }

    private static JPanel verticalPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        return panel;
    }

    private static void addCentered(JPanel column, JComponent component) {
        component.setAlignmentX(Component.CENTER_ALIGNMENT);
        column.add(component);
    }

    private static void addLeft(JPanel column, JComponent component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        column.add(component);
    }

    private static JPanel topAligned(JPanel content) {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        outer.add(content, BorderLayout.NORTH);
        return outer;
    }
}
