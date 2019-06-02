package me.waifu.graphquery;

import java.util.Collection;

class Util {

    // TODO de-hardcode this
    public static String getString(Object value, boolean asString) {
        if (value instanceof Number)
            return value.toString();
        else if (value instanceof Character || value instanceof CharSequence)
//            return (escapeStrings ? "\\" : "") + "\"" + value + (escapeStrings ? "\\" : "") + "\"";
            return formatString(String.valueOf(value), asString);
        else if (value instanceof Enum)
//            return (escapeStrings ? "\\" : "") + "\"" + ((Enum) value).name() + (escapeStrings ? "\\" : "") + "\"";
            return formatString(((Enum) value).name(), asString);
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

                    builder.append(getString(entry, asString));
                }
                builder.append("]");
                return builder.toString();
            }
        }
        else
            throw new IllegalArgumentException("Unknown type " + value.getClass().getName());
    }

    public static String formatString(CharSequence string, boolean asString) {
        return (asString ? "\"" : "") + (string.charAt(0) == '#' ? string.subSequence(1, string.length()) : string) + (asString ? "\"" : "");
    }

    public static boolean isVariable(Object object) {
        return object instanceof CharSequence && (object.toString().startsWith("$") || object.toString().startsWith("#"));
    }
}
