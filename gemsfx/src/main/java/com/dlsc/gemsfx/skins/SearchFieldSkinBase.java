package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.SearchField;

/**
 * Base skin for search-field based controls.
 * <p>
 * The class currently provides the common {@link GemsSkinBase} lifecycle for
 * controls derived from {@link SearchField}.
 *
 * @param <T> the skinned search-field control type
 */
public class SearchFieldSkinBase<T extends SearchField> extends GemsSkinBase<T> {

    /**
     * Creates a new base skin for the given search-field control.
     *
     * @param control the control to skin
     */
    public SearchFieldSkinBase(T control) {
        super(control);
    }
}
