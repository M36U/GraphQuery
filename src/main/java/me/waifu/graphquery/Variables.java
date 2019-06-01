package me.waifu.graphquery;

import java.util.*;

public final class Variables {

    private final Map<String, String> values;

    public Variables() {
        this.values = new HashMap<>();
    }

    public Variables add(String name, Object value) {
        values.put(name, Util.getString(value));
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
}
