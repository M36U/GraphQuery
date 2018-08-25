package me.waifu.graphquery;

import java.util.*;

public final class Variables {

    private final Map<String, String> values;

    public Variables() {
        this.values = new HashMap<>();
    }

    public Variables add(String name, Object value) {
        values.put(name, getString(value));
        return this;
    }

    public Variables reset() {
        values.clear();
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{ ");
        values.forEach((k, v) -> {
            if (builder.length() > 2)
                builder.append(",");

            builder.append("\"").append(k).append("\":").append(v);
        });

        builder.append(" }");
        return builder.toString();
    }

    // TODO de-hardcode this
    private static String getString(Object value) {
        if (value instanceof Number)
            return value.toString();
        else if (value instanceof Character || value instanceof CharSequence)
            return "\"" + value + "\"";
        else if (value instanceof Boolean)
            return value.toString();
        else if (value instanceof Collection) {
            Collection collection = (Collection) value;
            if (collection.isEmpty())
                return "[]";
            else {
                StringBuilder builder = new StringBuilder("[");
                for (Object entry : collection) {
                    if (builder.length() > 1)
                        builder.append(",");

                    builder.append(getString(entry));
                }
                builder.append("]");
                return builder.toString();
            }
        }
        else
            throw new IllegalArgumentException("Unknown type " + value.getClass().getName());
    }
}
