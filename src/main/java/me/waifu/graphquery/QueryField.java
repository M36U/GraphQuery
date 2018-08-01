package me.waifu.graphquery;

public class QueryField implements IQueryEntry<QueryField> {

    private final String fieldName;
    private String alias;
    private String includeVariable;
    private String skipVariable;

    public QueryField(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    @Override
    public void appendString(StringBuilder builder) {
        if (alias != null)
            builder.append(alias).append(": ");

        builder.append(" ").append(fieldName).append(" ");

        if (includeVariable != null)
            builder.append("@include(if: ").append(includeVariable).append(") ");
        if (skipVariable != null)
            builder.append("@skip(if: ").append(skipVariable).append(") ");
    }

    @Override
    public QueryField withAlias(String alias) {
        this.alias = alias;
        return this;
    }

    @Override
    public QueryField includeIf(String requiredVariable) {
        if (skipVariable != null)
            throw new IllegalStateException("Cannot have both include and skip on an entry");

        if (!requiredVariable.startsWith("$"))
            requiredVariable = "$" + requiredVariable;

        includeVariable = requiredVariable;
        return this;
    }

    @Override
    public QueryField skipIf(String requiredVariable) {
        if (includeVariable != null)
            throw new IllegalStateException("Cannot have both skip and include on an entry");

        if (!requiredVariable.startsWith("$"))
            requiredVariable = "$" + requiredVariable;

        skipVariable = requiredVariable;
        return this;
    }
}

