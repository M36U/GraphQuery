package me.waifu.graphquery;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;

public class GraphQLQuery {

    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final Map<String, Object> variables;
    private final QueryObject root;
    private final Set<QueryFragment> fragments;
    private URL requestUrl;

    public GraphQLQuery(Consumer<GraphQLQuery> $) {
        this.variables = new HashMap<>();
        this.root = new QueryObject("query");
        this.fragments = new HashSet<>();

        $.accept(this);
    }

    public FutureTask<String> submitRequest() throws IllegalStateException, IOException {
        if (requestUrl == null)
            throw new IllegalStateException("Request URL must be set.");

        HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");

        try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream())) {
            JsonObject queryRoot = new JsonObject();
            queryRoot.addProperty("query", toString());

            JsonObject variableObject = new JsonObject();
            variables.forEach((k, v) -> variableObject.add(k, GSON.toJsonTree(v)));
            queryRoot.add("variables", variableObject);

            writer.write(queryRoot.toString());
        }

        return new FutureTask<>(() -> {
            try {
                return readLines(connection.getInputStream());
            } catch (IOException e) {
                return readLines(connection.getErrorStream());
            }
        });
    }

    public GraphQLQuery withUrl(String url) throws MalformedURLException {
        this.requestUrl = new URL(url);
        return this;
    }

    public GraphQLQuery withVariable(String name, Object value) {
        variables.put(name, value);
        return this;
    }

    public GraphQLQuery withArgument(String name, String type) {
        if (!name.startsWith("$"))
            name = "$" + name;

        root.withArgument(name, type);
        return this;
    }

    public GraphQLQuery withObject(Consumer<QueryObject> $) {
        $.accept(root);
        return this;
    }

    public QueryFragment defineFragment(String name, String type, Consumer<QueryFragment> $) {
        QueryFragment fragment = new QueryFragment(name, type);
        $.accept(fragment);
        fragments.add(fragment);
        return fragment;
    }

    @Override
    public String toString() {
        StringBuilder queryBuilder = new StringBuilder();
        root.appendString(queryBuilder);
        queryBuilder.append(" ");
        fragments.forEach(f -> {
            f.appendString(queryBuilder);
            queryBuilder.append(" ");
        });
        return queryBuilder.toString().replaceAll("  ", " ");
    }

    private static String readLines(InputStream stream) {
        try (ByteArrayOutputStream result = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;

            while ((length = stream.read(buffer)) != -1)
                result.write(buffer, 0, length);

            return result.toString("UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }
}
