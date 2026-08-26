package com.dlsc.gemsfx.showcase;

import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.CupertinoLight;
import atlantafx.base.theme.Dracula;
import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.NordLight;
import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import atlantafx.base.theme.Theme;
import com.dlsc.atlantafx.themes.ArmyDark;
import com.dlsc.atlantafx.themes.ArmyLight;
import com.dlsc.atlantafx.themes.Autumn;
import com.dlsc.atlantafx.themes.Blacky;
import com.dlsc.atlantafx.themes.BlueDark;
import com.dlsc.atlantafx.themes.BlueLight;
import com.dlsc.atlantafx.themes.Browny;
import com.dlsc.atlantafx.themes.FallDark;
import com.dlsc.atlantafx.themes.FallLight;
import com.dlsc.atlantafx.themes.GithubDarkColorblind;
import com.dlsc.atlantafx.themes.GithubDarkTritanopia;
import com.dlsc.atlantafx.themes.GithubLightColorblind;
import com.dlsc.atlantafx.themes.GithubLightDefault;
import com.dlsc.atlantafx.themes.GithubLightTritanopia;
import com.dlsc.atlantafx.themes.GithubSoftDark;
import com.dlsc.atlantafx.themes.NavyDark;
import com.dlsc.atlantafx.themes.NavyLight;
import com.dlsc.atlantafx.themes.News;
import com.dlsc.atlantafx.themes.SpringDark;
import com.dlsc.atlantafx.themes.SpringLight;
import com.dlsc.atlantafx.themes.SummerDark;
import com.dlsc.atlantafx.themes.SummerLight;
import com.dlsc.atlantafx.themes.WinterDark;
import com.dlsc.atlantafx.themes.WinterLight;
import com.dlsc.atlantafx.themes.Yacht;

import java.util.List;

/**
 * A group of AtlantaFX themes that belong together. Most families consist of a light and a dark
 * variant, which allows the application to present them by their base name, e.g. "Nord" instead
 * of "Nord Light" and "Nord Dark". Some themes only exist in a single variant, in which case
 * either {@link #light()} or {@link #dark()} is {@code null} and the application hides the
 * control used for switching between the light and the dark color scheme.
 * <p>
 * The special family {@link #MODENA} represents the standard JavaFX theme. It does not provide
 * any themes at all, which is the signal for the {@link ShowcaseThemeManager} to switch back to
 * the default user agent stylesheet of JavaFX.
 *
 * @param name  the base name of the theme family, e.g. "Nord"
 * @param light the light variant of the theme, {@code null} if the family has no light variant
 * @param dark  the dark variant of the theme, {@code null} if the family has no dark variant
 */
public record ThemeFamily(String name, Theme light, Theme dark) {

    /**
     * The name of the theme family that will be used when no other family has been persisted.
     */
    public static final String DEFAULT_NAME = "Nord";

    /**
     * The standard JavaFX theme. Selecting this family switches the application back to the
     * default user agent stylesheet, meaning no AtlantaFX styling will be applied at all. The
     * family only exists in a light variant.
     */
    public static final ThemeFamily MODENA = new ThemeFamily("Modena", null, null);

    /**
     * All supported theme families. The standard JavaFX theme comes first, the AtlantaFX
     * families follow in alphabetical order. The "Github" family combines the default light
     * theme of GitHub with its dimmed ("soft") dark theme, as AtlantaFX does not ship a default
     * dark variant.
     */
    public static final List<ThemeFamily> ALL_FAMILIES = List.of(
            MODENA,
            new ThemeFamily("Army", new ArmyLight(), new ArmyDark()),
            new ThemeFamily("Autumn", null, new Autumn()),
            new ThemeFamily("Blacky", null, new Blacky()),
            new ThemeFamily("Blue", new BlueLight(), new BlueDark()),
            new ThemeFamily("Browny", null, new Browny()),
            new ThemeFamily("Cupertino", new CupertinoLight(), new CupertinoDark()),
            new ThemeFamily("Dracula", null, new Dracula()),
            new ThemeFamily("Fall", new FallLight(), new FallDark()),
            new ThemeFamily("Github", new GithubLightDefault(), new GithubSoftDark()),
            new ThemeFamily("Github Colorblind", new GithubLightColorblind(), new GithubDarkColorblind()),
            new ThemeFamily("Github Tritanopia", new GithubLightTritanopia(), new GithubDarkTritanopia()),
            new ThemeFamily("Navy", new NavyLight(), new NavyDark()),
            new ThemeFamily("News", null, new News()),
            new ThemeFamily(DEFAULT_NAME, new NordLight(), new NordDark()),
            new ThemeFamily("Primer", new PrimerLight(), new PrimerDark()),
            new ThemeFamily("Spring", new SpringLight(), new SpringDark()),
            new ThemeFamily("Summer", new SummerLight(), new SummerDark()),
            new ThemeFamily("Winter", new WinterLight(), new WinterDark()),
            new ThemeFamily("Yacht", new Yacht(), null)
    );

    /**
     * Returns the theme family with the given base name or the default family ("Nord") if no
     * such family exists.
     *
     * @param name the base name of the family
     * @return the matching family, never {@code null}
     */
    public static ThemeFamily findByName(String name) {
        return ALL_FAMILIES.stream()
                .filter(family -> family.name().equals(name))
                .findFirst()
                .orElseGet(ThemeFamily::getDefault);
    }

    public static ThemeFamily getDefault() {
        return ALL_FAMILIES.stream()
                .filter(family -> family.name().equals(DEFAULT_NAME))
                .findFirst()
                .orElseThrow();
    }

    /**
     * Returns the theme to use for the given "darkness". Families that only exist in a single
     * variant always return that variant, no matter what has been requested.
     *
     * @param dark if true the dark variant will be returned, otherwise the light variant
     * @return the theme matching the requested variant, {@code null} for {@link #MODENA}
     */
    public Theme getTheme(boolean dark) {
        if (isModena()) {
            return null;
        }

        if (!hasBothVariants()) {
            return light != null ? light : this.dark;
        }

        return dark ? this.dark : light;
    }

    /**
     * Determines whether this family comes with a light and a dark variant. Only in that case
     * does it make sense to offer the user a choice between the light, the dark, and the system
     * color scheme.
     *
     * @return true if both variants exist
     */
    public boolean hasBothVariants() {
        return light != null && dark != null;
    }

    /**
     * Determines whether the single variant of this family is a dark theme. The result is only
     * meaningful for families that do not have {@link #hasBothVariants() both variants}.
     *
     * @return true if the only variant of this family is a dark theme
     */
    public boolean isDarkOnly() {
        return light == null && dark != null;
    }

    /**
     * Determines whether this family represents the standard JavaFX theme, which means that no
     * AtlantaFX theme will be applied at all.
     *
     * @return true if this is the {@link #MODENA} family
     */
    public boolean isModena() {
        return light == null && dark == null;
    }
}
