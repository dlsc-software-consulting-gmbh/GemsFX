package com.dlsc.gemsfx.gridtable;

import javafx.util.Callback;

import java.lang.reflect.Field;
import java.util.Objects;

public class GridTablePropertyValueFactory<S, T> implements Callback<S, T> {

    private final String property;

    private Field field;

    public GridTablePropertyValueFactory(String propertyName) {
        this.property = Objects.requireNonNull(propertyName);
    }

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
