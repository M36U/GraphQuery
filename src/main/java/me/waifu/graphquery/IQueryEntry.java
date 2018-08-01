package me.waifu.graphquery;

public interface IQueryEntry<T extends IQueryEntry<T>> {

    void appendString(StringBuilder builder);

    T withAlias(String alias);

    T includeIf(String requiredVariable);

    T skipIf(String requiredVariable);
}
