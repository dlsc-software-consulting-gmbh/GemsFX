package com.dlsc.gemsfx.showcase;

import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.CupertinoLight;
import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.NordLight;
import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import atlantafx.base.theme.Theme;
import com.dlsc.atlantafx.themes.ArmyDark;
import com.dlsc.atlantafx.themes.ArmyLight;
import com.dlsc.atlantafx.themes.BlueDark;
import com.dlsc.atlantafx.themes.BlueLight;
import com.dlsc.atlantafx.themes.FallDark;
import com.dlsc.atlantafx.themes.FallLight;
import com.dlsc.atlantafx.themes.NavyDark;
import com.dlsc.atlantafx.themes.NavyLight;
import com.dlsc.atlantafx.themes.SpringDark;
import com.dlsc.atlantafx.themes.SpringLight;
import com.dlsc.atlantafx.themes.SummerDark;
import com.dlsc.atlantafx.themes.SummerLight;
import com.dlsc.atlantafx.themes.WinterDark;
import com.dlsc.atlantafx.themes.WinterLight;

import java.util.List;

/**
 * A pair of AtlantaFX themes that belong together, one for the light and one for the dark
 * variant. Only themes that exist in both variants are supported by the showcase, which allows
 * the application to present them by their base name, e.g. "Nord" instead of "Nord Light" and
 * "Nord Dark".
 *
 * @param name  the base name of the theme family, e.g. "Nord"
 * @param light the light variant of the theme
 * @param dark  the dark variant of the theme
 */
public record ThemeFamily(String name, Theme light, Theme dark) {

    /**
     * The name of the theme family that will be used when no other family has been persisted.
     */
    public static final String DEFAULT_NAME = "Nord";

    /**
     * All supported theme families, in alphabetical order.
     */
    public static final List<ThemeFamily> ALL_FAMILIES = List.of(
            new ThemeFamily("Army", new ArmyLight(), new ArmyDark()),
            new ThemeFamily("Blue", new BlueLight(), new BlueDark()),
            new ThemeFamily("Cupertino", new CupertinoLight(), new CupertinoDark()),
            new ThemeFamily("Fall", new FallLight(), new FallDark()),
            new ThemeFamily("Navy", new NavyLight(), new NavyDark()),
            new ThemeFamily(DEFAULT_NAME, new NordLight(), new NordDark()),
            new ThemeFamily("Primer", new PrimerLight(), new PrimerDark()),
            new ThemeFamily("Spring", new SpringLight(), new SpringDark()),
            new ThemeFamily("Summer", new SummerLight(), new SummerDark()),
            new ThemeFamily("Winter", new WinterLight(), new WinterDark())
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
     * Returns the theme to use for the given "darkness".
     *
     * @param dark if true the dark variant will be returned, otherwise the light variant
     * @return the theme matching the requested variant
     */
    public Theme getTheme(boolean dark) {
        return dark ? this.dark : light;
    }
}
