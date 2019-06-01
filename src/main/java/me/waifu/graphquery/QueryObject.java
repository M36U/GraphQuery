package me.waifu.graphquery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class QueryObject implements IQueryEntry<QueryObject> {

    protected final String name;
    protected final List<IQueryEntry> entries;
    private final Map<String, Object> arguments;
    protected String alias;
    protected String includeVariable;
    protected String skipVariable;

    QueryObject(String name) {
        this.name = name;
        this.entries = new ArrayList<>();
        this.arguments = new HashMap<>();
    }

    public QueryObject withArgument(String name, Object value) {
        arguments.put(name, value);
        return this;
    }

    public QueryField withField(String name) {
        QueryField field = new QueryField(name);
        entries.add(field);
        return field;
    }

    public QueryField withFragment(String name) {
        if (!name.startsWith("..."))
            name = "..." + name;

        return withField(name);
    }

    public QueryFragment withFragment(String type, Consumer<QueryFragment> $) {
        QueryFragment fragment = new QueryFragment("...", type);
        fragment.inline = true;
        $.accept(fragment);
        entries.add(fragment);
        return fragment;
    }

    public QueryObject withObject(String name, Consumer<QueryObject> $) {
        QueryObject object = new QueryObject(name);
        $.accept(object);
        entries.add(object);
        return object;
    }

    @Override
    public void appendString(StringBuilder builder) {
        if (alias != null)
            builder.append(alias).append(": ");

        builder.append(name);
        if (!arguments.isEmpty()) {
            builder.append("(");
            arguments.forEach((k, v) -> builder.append(k).append(": ").append(Util.getString(v)).append(", "));
            if (builder.charAt(builder.length() - 1) == ' ')
                builder.delete(builder.length() - 2, builder.length());

            builder.append(")");
        }

        if (includeVariable != null)
            builder.append(" @include(if: ").append(includeVariable).append(") ");
        if (skipVariable != null)
            builder.append(" @skip(if: ").append(skipVariable).append(") ");

        builder.append(" { ");
        entries.forEach(e -> e.appendString(builder));
        builder.append(" } ");
    }

    @Override
    public QueryObject withAlias(String alias) {
        this.alias = alias;
        return this;
    }

    @Override
    public QueryObject includeIf(String requiredVariable) {
        if (skipVariable != null)
            throw new IllegalStateException("Cannot have both include and skip on an entry");

        if (!requiredVariable.startsWith("$"))
            requiredVariable = "$" + requiredVariable;

        includeVariable = requiredVariable;
        return this;
    }

    @Override
    public QueryObject skipIf(String requiredVariable) {
        if (includeVariable != null)
            throw new IllegalStateException("Cannot have both skip and include on an entry");

        if (!requiredVariable.startsWith("$"))
            requiredVariable = "$" + requiredVariable;

        skipVariable = requiredVariable;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        appendString(builder);
        return builder.toString().trim();
    }
}
