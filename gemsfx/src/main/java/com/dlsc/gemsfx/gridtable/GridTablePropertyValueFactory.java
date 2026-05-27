package com.dlsc.gemsfx.gridtable;

import javafx.util.Callback;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * A {@link javafx.util.Callback} implementation that extracts a property value from
 * a row item using Java reflection, similar in purpose to
 * {@link javafx.scene.control.cell.PropertyValueFactory} in the standard JavaFX
 * {@code TableView}.
 *
 * <p>The target field is located by name in the item class hierarchy (including
 * superclasses) on the first invocation and cached for subsequent calls. The field
 * is made accessible if necessary.
 *
 * @param <S> the type of the row item
 * @param <T> the type of the cell value to extract
 */
public class GridTablePropertyValueFactory<S, T> implements Callback<S, T> {

    private final String property;

    private Field field;

    /**
     * Constructs a new property value factory.
     *
     * @param propertyName the field name to read from row items
     */
    public GridTablePropertyValueFactory(String propertyName) {
        this.property = Objects.requireNonNull(propertyName);
    }

    /**
     * Extracts the configured field value from the given row item.
     *
     * @param rowItem the row item
     * @return the extracted value
     */
    @Override
    public T call(S rowItem) {
        if (field == null) {
            Class<?> clazz = rowItem.getClass();
            while (clazz != null) {
                Field[] fields = clazz.getDeclaredFields();
                for (Field f : fields) {
                    if (f.getName().equals(property)) {
                        f.setAccessible(true);
                        field = f;
                        break;
                    }
                }

                clazz = clazz.getSuperclass();
            }
        }

        if (field != null) {
            try {
                return (T) field.get(rowItem);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        throw new RuntimeException("Property " + property + " not found in Object: " + rowItem);
    }
}
