package entity;

import java.util.Objects;

/**
 * Immutable value object describing how the app should be displayed: theme,
 * font, and contrast. Two instances with equal fields are interchangeable.
 */
public final class AppearanceSettings {

    public static final int MIN_FONT_SIZE = 10;

    public static final int MAX_FONT_SIZE = 24;

    private static final String DEFAULT_FONT_FAMILY = "SansSerif";

    private static final int DEFAULT_FONT_SIZE = 14;

    private final Theme theme;

    private final String fontFamily;

    private final int fontSize;

    private final boolean highContrast;

    public AppearanceSettings(Theme theme, String fontFamily, int fontSize, boolean highContrast) {
        this.theme = Objects.requireNonNull(theme, "theme is required.");
        this.fontFamily = normalizeFontFamily(fontFamily);
        if (fontSize < MIN_FONT_SIZE || fontSize > MAX_FONT_SIZE) {
            throw new IllegalArgumentException(
                    "fontSize must be between " + MIN_FONT_SIZE + " and " + MAX_FONT_SIZE);
        }
        this.fontSize = fontSize;
        this.highContrast = highContrast;
    }

    /** Light theme, default font, default size, no high contrast. */
    public static AppearanceSettings defaults() {
        return new AppearanceSettings(Theme.LIGHT, DEFAULT_FONT_FAMILY, DEFAULT_FONT_SIZE, false);
    }

    // Null or blank font family falls back to the default rather than being rejected;
    // fontFamily has no required-value meaning the way theme does.
    private static String normalizeFontFamily(String fontFamily) {
        if (fontFamily == null || fontFamily.trim().isEmpty()) {
            return DEFAULT_FONT_FAMILY;
        }
        return fontFamily.trim();
    }

    public Theme theme() {
        return theme;
    }

    public String fontFamily() {
        return fontFamily;
    }

    public int fontSize() {
        return fontSize;
    }

    public boolean highContrast() {
        return highContrast;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AppearanceSettings)) {
            return false;
        }
        AppearanceSettings that = (AppearanceSettings) other;
        return fontSize == that.fontSize
                && highContrast == that.highContrast
                && theme == that.theme
                && fontFamily.equals(that.fontFamily);
    }

    @Override
    public int hashCode() {
        return Objects.hash(theme, fontFamily, fontSize, highContrast);
    }
}
