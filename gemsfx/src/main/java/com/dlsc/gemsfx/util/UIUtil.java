package com.dlsc.gemsfx.util;


import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class UIUtil {

    private UIUtil() {
    }

    /**
     * Adds a style class to a node if it is not already present.
     *
     * @param node       The node to add the style class to.
     * @param styleClass The style class to add.
     */
    public static void addClassIfAbsent(Node node, String styleClass) {
        Optional.ofNullable(node).ifPresent(n -> {
            if (!n.getStyleClass().contains(styleClass)) {
                n.getStyleClass().add(styleClass);
            }
        });
    }

    /**
     * Adds a list of style classes to a node if they are not already present.
     *
     * @param node         The node to add the style classes to.
     * @param styleClasses The style classes to add.
     */
    public static void addClassesIfAbsent(Node node, String... styleClasses) {
        List<String> list = Arrays.stream(styleClasses)
                .filter(styleClass -> !node.getStyleClass().contains(styleClass))
                .toList();
        node.getStyleClass().addAll(list);
    }

    /**
     * Toggles a style class on a node.
     * If the style class is present, it is removed.
     * If it is not present, it is added.
     *
     * @param node       The node to toggle the style on.
     * @param styleClass The style class to add or remove.
     */
    public static void toggleClass(Node node, String styleClass) {
        if (node.getStyleClass().contains(styleClass)) {
            node.getStyleClass().remove(styleClass);
        } else {
            node.getStyleClass().add(styleClass);
        }
    }

    /**
     * Toggles a style class on a node based on a condition.
     * If the condition is true, the style class is added.
     * If the condition is false, the style class is removed.
     *
     * @param node       The node to toggle the style on.
     * @param styleClass The style class to add or remove.
     * @param condition  The condition that determines whether to add or remove the style.
     */
    public static void toggleClassOnCondition(Node node, String styleClass, boolean condition) {
        if (condition) {
            addClassIfAbsent(node, styleClass);
        } else {
            node.getStyleClass().remove(styleClass);
        }
    }


    /**
     * Optimizes style updates for a given node by first adding a specified style to ensure it's present,
     * and then removing other specified styles, except the newly added one. This approach helps in preventing
     * unnecessary UI flicker by avoiding the removal of a style that needs to be present.
     *
     * @param node           The node whose styles are to be updated.
     * @param stylesToRemove A list of styles to be removed from the node, except for the styleToAdd.
     * @param styleToAdd     The style to be added to the node, if it's not already present.
     */
    public static void updateStyles(Node node, List<String> stylesToRemove, String styleToAdd) {
        // Add the style if it's not already present
        addClassIfAbsent(node, styleToAdd);

        // Remove the specified styles except the style to be added
        node.getStyleClass().removeAll(stylesToRemove.stream()
                .filter(style -> !style.equals(styleToAdd))
                .toList());
    }

    /**
     * Optimizes style updates for a given node by first adding a specified style to ensure it's present,
     * and then removing other specified styles, except the newly added one. This approach helps in preventing
     * unnecessary UI flicker by avoiding the removal of a style that needs to be present.
     *
     * @param node           The node whose styles are to be updated.
     * @param stylesToRemove An array of styles to be removed from the node, except for the styleToAdd.
     * @param styleToAdd     The style to be added to the node, if it's not already present.
     */
    public static void updateStyles(Node node, String[] stylesToRemove, String styleToAdd) {
        updateStyles(node, Arrays.asList(stylesToRemove), styleToAdd);
    }

    /**
     * Applies a style derived from an enum value to a Node, removing other styles from the same enum class.
     *
     * @param node      The Node to update.
     * @param enumValue The enum value determining the style to apply.
     *                  <p> Example     If Dir.UP is passed, add "up" style and removes {"down", "left", "right"} styles.
     */
    public static <T extends Enum<T>> void updateStyleFromEnum(Node node, T enumValue) {
        updateStyles(node, EnumUtil.convertAllToStylesClassName(enumValue.getClass()), EnumUtil.convertToStyleClassName(enumValue));
    }

    /**
     * Removes all styles associated with a given enum class from a Node.
     *
     * @param node      The Node to clear styles from.
     * @param enumClass The enum class whose associated styles will be removed.
     *                  <p> Example     If Dir.class is passed, removes all styles {"up","down","left", "right"}.
     */
    public static <T extends Enum<T>> void clearStylesByEnum(Node node, Class<T> enumClass) {
        node.getStyleClass().removeAll(EnumUtil.convertAllToStylesClassName(enumClass));
    }

    /**
     * Returns the height of the top and bottom insets combined.
     */
    public static double getInsetsHeight(Insets insets) {
        return insets == null ? 0 : insets.getTop() + insets.getBottom();
    }

    /**
     * Returns the width of the left and right insets combined.
     */
    public static double getInsetsWidth(Insets insets) {
        return insets == null ? 0 : insets.getLeft() + insets.getRight();
    }

    /**
     * Converts a camelCase string to a natural language expression.
     * <p>
     * This method is designed to transform strings formatted in camelCase,
     * commonly used in programming, into a more readable format that is
     * suitable for display in user interfaces. It inserts spaces between
     * words and ensures that acronyms remain capitalized while the first
     * letter of the resulting string is also capitalized.
     * </p>
     * <p>
     * Example:
     * "ONSCode" becomes "ONS Code"
     * "customerServiceOrder" becomes "Customer Service Order"
     * </p>
     *
     * @param camelCaseString The camelCase string to be converted.
     * @return A string in natural language format, with appropriate spaces and capitalization.
     */
    public static String camelCaseToNaturalCase(String camelCaseString) {
        return StringUtils.capitalize(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(camelCaseString), " "));
    }

    /**
     * Copies the specified content to the clipboard.
     *
     * @param copyContent The content to be copied to the clipboard.
     */
    public static void copyToClipboard(String copyContent) {
        ClipboardContent content = new ClipboardContent();
        content.putString(copyContent);
        Clipboard.getSystemClipboard().setContent(content);
    }

    /**
     * Determines if the given mouse event is a single primary button click
     * that hasn't moved since it was pressed.
     *
     * <p>This method checks if the mouse event satisfies the following conditions:
     * <ul>
     *   <li>The mouse button is the primary button (usually the left button).</li>
     *   <li>The click count is exactly one.</li>
     *   <li>The mouse has not moved since it was pressed.</li>
     * </ul>
     *
     * @param event The mouse event to check. Must not be null.
     * @return {@code true} if the event is a single stable primary button click, {@code false} otherwise.
     * @throws NullPointerException if the event is null.
     */
    public static boolean clickOnNode(MouseEvent event) {
        return event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1 && event.isStillSincePress();
    }

}