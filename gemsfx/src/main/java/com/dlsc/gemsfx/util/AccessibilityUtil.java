package com.dlsc.gemsfx.util;

import javafx.beans.value.ObservableValue;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;

import java.util.Objects;

/**
 * Small helper methods used by the GemsFX controls to add a baseline of
 * accessibility metadata (screen-reader support).
 * <p>
 * The GemsFX convention is:
 * <ul>
 *     <li>every user-facing control sets an {@link AccessibleRole} in its constructor, and</li>
 *     <li>value-bearing controls derive their {@code accessibleText} from their own state.</li>
 * </ul>
 * The {@code accessibleText} helpers deliberately <b>yield to application code</b>: once an
 * application explicitly calls {@link Node#setAccessibleText(String)} with a value that this
 * class did not write, the automatic updates stop so the application value always wins.
 */
public final class AccessibilityUtil {

    private AccessibilityUtil() {
    }

    /**
     * Sets the accessible role on the given node.
     *
     * @param node the node, may be {@code null} (ignored)
     * @param role the accessible role
     */
    public static void setRole(Node node, AccessibleRole role) {
        if (node != null && role != null) {
            node.setAccessibleRole(role);
        }
    }

    /**
     * Sets the accessible role and role description on the given node. The role description is a
     * short, localized, human-readable name for the type of control (for example "avatar").
     *
     * @param node            the node, may be {@code null} (ignored)
     * @param role            the accessible role
     * @param roleDescription a human-readable description of the role
     */
    public static void setRole(Node node, AccessibleRole role, String roleDescription) {
        setRole(node, role);
        if (node != null && roleDescription != null) {
            node.setAccessibleRoleDescription(roleDescription);
        }
    }

    /**
     * Keeps the {@code accessibleText} of the given node in sync with the value of the given
     * observable, without overriding a value that the application has set itself.
     * <p>
     * The node's accessible text is updated whenever the source changes, but only as long as the
     * current accessible text is still the value this method last wrote (or is empty). As soon as
     * application code sets a different accessible text, the automatic updates stop.
     *
     * @param node   the node whose accessible text should be maintained, may be {@code null}
     * @param source the observable providing the accessible text
     */
    public static void bindAccessibleText(Node node, ObservableValue<String> source) {
        if (node == null || source == null) {
            return;
        }

        // holds the last value this helper wrote, so we can detect application overrides
        String[] lastWritten = {null};

        Runnable update = () -> {
            String current = node.getAccessibleText();
            boolean appHasOverridden = current != null && !current.isEmpty() && !Objects.equals(current, lastWritten[0]);
            if (!appHasOverridden) {
                String value = source.getValue();
                lastWritten[0] = value;
                node.setAccessibleText(value);
            }
        };

        source.addListener((obs, oldValue, newValue) -> update.run());
        update.run();
    }
}
