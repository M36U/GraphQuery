package me.waifu.graphquery;

import java.util.Collection;

class Util {

    // TODO de-hardcode this
    public static String getString(Object value) {
        if (value instanceof Number)
            return value.toString();
        else if (value instanceof Character || value instanceof CharSequence)
            return "\\\"" + value + "\\\"";
        else if (value instanceof Enum)
            return "\\\"" + ((Enum) value).name() + "\\\"";
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
