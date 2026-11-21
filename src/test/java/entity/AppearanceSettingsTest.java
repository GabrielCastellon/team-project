package entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class AppearanceSettingsTest {

    @Test
    void defaultsAreLightSansSerifFourteenNoHighContrast() {
        AppearanceSettings settings = AppearanceSettings.defaults();
        assertEquals(Theme.LIGHT, settings.theme());
        assertEquals("SansSerif", settings.fontFamily());
        assertEquals(14, settings.fontSize());
        assertFalse(settings.highContrast());
    }

    @Test
    void constructorStoresAllFields() {
        AppearanceSettings settings = new AppearanceSettings(Theme.DARK, "Serif", 18, true);
        assertEquals(Theme.DARK, settings.theme());
        assertEquals("Serif", settings.fontFamily());
        assertEquals(18, settings.fontSize());
        assertTrue(settings.highContrast());
    }

    //BR: theme is required; construction rejects null
    @Test
    void rejectNullTheme() {
        assertThrows(NullPointerException.class,
                () -> new AppearanceSettings(null, "Serif", 14, false));
    }

    //BR: a missing font family falls back to the default instead of failing
    @Test
    void nullFontFamilyDefaultsToSansSerif() {
        AppearanceSettings settings = new AppearanceSettings(Theme.LIGHT, null, 14, false);
        assertEquals("SansSerif", settings.fontFamily());
    }

    //BR: a blank font family also falls back to the default
    @Test
    void blankFontFamilyDefaultsToSansSerif() {
        AppearanceSettings settings = new AppearanceSettings(Theme.LIGHT, "   ", 14, false);
        assertEquals("SansSerif", settings.fontFamily());
    }

    //BR: fontSize accepts its documented boundaries
    @Test
    void fontSizeAcceptsBoundaryValues() {
        AppearanceSettings min = new AppearanceSettings(
                Theme.LIGHT, "Serif", AppearanceSettings.MIN_FONT_SIZE, false);
        AppearanceSettings max = new AppearanceSettings(
                Theme.LIGHT, "Serif", AppearanceSettings.MAX_FONT_SIZE, false);
        assertEquals(AppearanceSettings.MIN_FONT_SIZE, min.fontSize());
        assertEquals(AppearanceSettings.MAX_FONT_SIZE, max.fontSize());
    }

    //BR: fontSize rejects values just outside its documented boundaries
    @Test
    void fontSizeRejectsJustOutsideBoundaries() {
        assertThrows(IllegalArgumentException.class,
                () -> new AppearanceSettings(
                        Theme.LIGHT, "Serif", AppearanceSettings.MIN_FONT_SIZE - 1, false));
        assertThrows(IllegalArgumentException.class,
                () -> new AppearanceSettings(
                        Theme.LIGHT, "Serif", AppearanceSettings.MAX_FONT_SIZE + 1, false));
    }

    @Test
    void equalSettingsAreEqualAndHaveSameHashCode() {
        AppearanceSettings a = new AppearanceSettings(Theme.DARK, "Serif", 18, true);
        AppearanceSettings b = new AppearanceSettings(Theme.DARK, "Serif", 18, true);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void differingFieldMakesSettingsUnequal() {
        AppearanceSettings a = new AppearanceSettings(Theme.DARK, "Serif", 18, true);
        AppearanceSettings b = new AppearanceSettings(Theme.LIGHT, "Serif", 18, true);
        assertNotEquals(a, b);
    }

    @Test
    void notEqualToNullOrUnrelatedType() {
        AppearanceSettings a = AppearanceSettings.defaults();
        assertNotEquals(a, null);
        assertNotEquals(a, "not settings");
    }
}
