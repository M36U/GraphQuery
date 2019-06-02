package me.waifu.graphquery;

public class QueryFragment extends QueryObject {

    private final String type;
    protected boolean inline = false;

    public QueryFragment(String name, String type) {
        super(name, false);

        this.type = type;
    }

    @Override
    public void appendString(StringBuilder builder) {
        if (!inline)
            builder.append("fragment ");

        if (inline && alias != null)
            builder.append(alias).append(": ");

        builder.append(name).append(" on ").append(type);

        if (inline) {
            if (includeVariable != null)
                builder.append(" @include(if: ").append(includeVariable).append(") ");
            if (skipVariable != null)
                builder.append(" @skip(if: ").append(skipVariable).append(") ");
        }

        builder.append("{ ");
        entries.forEach(e -> e.appendString(builder));
        builder.append(" } ");
    }
}
