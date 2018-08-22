package me.waifu.graphquery;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;

public class GraphQLQuery {

    private final Variables variables;
    private final QueryObject root;
    private final Set<QueryFragment> fragments;
    private URL requestUrl;
    private boolean resetOnSubmit = true;

    public GraphQLQuery(RequestType requestType, Consumer<GraphQLQuery> $) {
        this.variables = new Variables();
        this.root = new QueryObject(requestType.toString());
        this.fragments = new HashSet<>();

        $.accept(this);
    }

    /**
     * Creates a runnable {@link FutureTask} that will submit a query and provide the response.
     *
     * If an error is received, the error response will be returned.
     *
     * @return a runnable future that will submit a query and return the response.
     * @throws IllegalStateException if {@link #requestUrl} is not set.
     * @throws IOException if the query body could not be written to.
     */
    public FutureTask<String> createRequest() throws IllegalStateException, IOException {
        if (requestUrl == null)
            throw new IllegalStateException("Request URL must be set.");

        HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");

        try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream())) {
            writer.write(getQueryString());
        }

        if (resetOnSubmit)
            variables.reset();

        return new FutureTask<>(() -> {
            try {
                return readLines(connection.getInputStream());
            } catch (IOException e) {
                return readLines(connection.getErrorStream());
            }
        });
    }

    /**
     * Sets the URL the query will be sent to when using {@link #createRequest()}.
     *
     * @param url the URL to send the query to.
     * @return this instance for chaining.
     * @throws MalformedURLException if the URL could not be properly parsed.
     */
    public GraphQLQuery withUrl(String url) throws MalformedURLException {
        this.requestUrl = new URL(url);
        return this;
    }

    public GraphQLQuery resetOnSubmit(boolean resetOnSubmit) {
        this.resetOnSubmit = resetOnSubmit;
        return this;
    }

    /**
     * Adds an argument to this object.
     *
     * @param name the name of this argument.
     * @param type the type of this argument.
     * @return this instance for chaining.
     */
    public GraphQLQuery withArgument(String name, String type) {
        if (!name.startsWith("$"))
            name = "$" + name;

        root.withArgument(name, type);
        return this;
    }

    /**
     * Defines a variable to send along with the query.
     *
     * @param name the name of the variable .
     * @param value the value of this variable. See {@link Variables#getString(Object)} for the types currently supported.
     * @return this instance for chaining.
     */
    public GraphQLQuery withVariable(String name, Object value) {
        variables.add(name, value);
        return this;
    }

    /**
     * Defines a new object ready for modification.
     * @param $ a consumer used to define the values of the created object.
     * @return this instance for chaining.
     */
    public GraphQLQuery withObject(Consumer<QueryObject> $) {
        $.accept(root);
        return this;
    }

    /**
     * Defines a new fragment type.
     *
     * @param name the name of this fragment.
     * @param type the type of this fragment.
     * @param $ a consumer used to define the values of the created fragment.
     * @return the created fragment object.
     */
    public QueryFragment defineFragment(String name, String type, Consumer<QueryFragment> $) {
        QueryFragment fragment = new QueryFragment(name, type);
        $.accept(fragment);
        fragments.add(fragment);
        return fragment;
    }

    /**
     * Creates the query JSON that will be sent to the server.
     *
     * @return the query JSON to send to the server.
     */
    public String getQueryString() {
        return String.format("{\"query\":\"%s\",\"variables\":%s}", toString(), variables.toString());
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
        return queryBuilder.toString().replaceAll("  ", " ").trim();
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

    public enum RequestType {
        QUERY,
        MUTATION,
        ;

        private final String name;

        RequestType() {
            this.name = name().toLowerCase(Locale.ROOT);
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
