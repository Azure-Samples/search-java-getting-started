package com.microsoft.azure.search.samples;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SearchIndexClient {
    private static final String API_VERSION = "2016-09-01";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String serviceName;
    private final String indexName;
    private final String apiKey;

    static {
        OBJECT_MAPPER.setDateFormat(new ISO8601DateFormat());
    }

    public SearchIndexClient(String serviceName, String indexName, String apiKey) {
        this.serviceName = serviceName;
        this.indexName = indexName;
        this.apiKey = apiKey;
    }

    public boolean exists() throws IOException {
        HttpURLConnection connection = httpRequest(buildIndexDefinitionUrl(), "GET");
        int response = connection.getResponseCode();
        if (response == 404) {
            return false;
        }
        throwOnHttpError(connection);
        return true;
    }

    public void create(IndexDefinition indexDefinition) throws IOException {
        if (indexDefinition.getName() == null) {
            indexDefinition.setName(this.indexName);
        }
        HttpURLConnection connection = httpRequest(buildIndexListUrl(), "POST");
        connection.setDoOutput(true);
        OBJECT_MAPPER.writeValue(connection.getOutputStream(), indexDefinition);
        throwOnHttpError(connection);
    }

    public void createOrUpdate(IndexDefinition indexDefinition) throws IOException {
        if (indexDefinition.getName() == null) {
            indexDefinition.setName(this.indexName);
        }
        HttpURLConnection connection = httpRequest(buildIndexDefinitionUrl(), "PUT");
        connection.setDoOutput(true);
        OBJECT_MAPPER.writeValue(connection.getOutputStream(), indexDefinition);
        throwOnHttpError(connection);
    }

    public boolean delete() throws IOException {
        HttpURLConnection connection = httpRequest(buildIndexDefinitionUrl(), "DELETE");
        int response = connection.getResponseCode();
        if (response == 404) {
            return false;
        }
        throwOnHttpError(connection);
        return true;
    }

    public IndexBatchResult indexBatch(final Collection<IndexBatchOperation> operations) throws IOException {
        return withHttpRetry(new RetriableHttpOperation<IndexBatchResult>() {
            @Override
            public IndexBatchResult run() throws HttpRetryException, IOException {
                HttpURLConnection connection = httpRequest(buildIndexingUrl(), "POST");
                connection.setDoOutput(true);
                IndexBatch batch = new IndexBatch();
                batch.getValue().addAll(operations);
                OBJECT_MAPPER.writeValue(connection.getOutputStream(), batch);
                throwOnHttpError(connection);
                IndexBatchResult result = OBJECT_MAPPER.readValue(connection.getInputStream(), IndexBatchResult.class);
                return result;
            }
        });
    }

    public IndexSearchResult search(final String search, final IndexSearchOptions options) throws IOException {
        return withHttpRetry(new RetriableHttpOperation<IndexSearchResult>() {
            @Override
            public IndexSearchResult run() throws HttpRetryException, IOException {
                HttpURLConnection connection = httpRequest(buildIndexSearchUrl(search, options), "GET");
                throwOnHttpError(connection);
                IndexSearchResult result = OBJECT_MAPPER.readValue(connection.getInputStream(), IndexSearchResult.class);
                return result;
            }
        });
    }

    public IndexSuggestResult suggest(final String search, final String suggesterName, final IndexSuggestOptions options) throws IOException {
        return withHttpRetry(new RetriableHttpOperation<IndexSuggestResult>() {
            @Override
            public IndexSuggestResult run() throws HttpRetryException, IOException {
                HttpURLConnection connection = httpRequest(buildIndexSuggestUrl(search, suggesterName, options), "GET");
                throwOnHttpError(connection);
                IndexSuggestResult result = OBJECT_MAPPER.readValue(connection.getInputStream(), IndexSuggestResult.class);
                return result;
            }
        });
    }

    public Map<String, Object> lookup(final String key) throws IOException {
        return withHttpRetry(new RetriableHttpOperation<Map<String, Object>>() {
            @Override
            public Map<String, Object> run() throws HttpRetryException, IOException {
                HttpURLConnection connection = httpRequest(buildIndexLookupUrl(key), "GET");
                throwOnHttpError(connection);
                Map<String, Object> document = OBJECT_MAPPER.readValue(connection.getInputStream(), new TypeReference<Map<String, Object>>() {
                });
                document.remove("@odata.context");
                return document;
            }
        });
    }

    private HttpURLConnection httpRequest(String url, String method) throws IOException {
        URL actualUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection)actualUrl.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("content-type", "application/json");
        connection.setRequestProperty("api-key", this.apiKey);
        return connection;
    }

    private void throwOnHttpError(HttpURLConnection connection) throws IOException {
        int code = connection.getResponseCode();
        if (code > 399) {
            if (code == 503) { // this typically means the server is asking for backoff + retry
                String message = String.format("HTTP error. Code: %s. Message: %s", code, getResponseString(connection));
                throw new HttpRetryException(message, code);
            }
            else {
                String message = String.format("HTTP error. Code: %s. Message: %s", code, getResponseString(connection));
                throw new RuntimeException(message);
            }
        }
    }

    private String buildIndexListUrl() {
        return String.format("https://%s.search.windows.net/indexes?api-version=%s", this.serviceName, API_VERSION);
    }

    private String buildIndexDefinitionUrl() {
        return String.format("https://%s.search.windows.net/indexes/%s?api-version=%s", this.serviceName, this.indexName, API_VERSION);
    }

    private String buildIndexingUrl() {
        return String.format("https://%s.search.windows.net/indexes/%s/docs/index?api-version=%s", this.serviceName, this.indexName, API_VERSION);
    }

    private String buildIndexLookupUrl(String key) throws IOException {
        return String.format("https://%s.search.windows.net/indexes/%s/docs('%s')?api-version=%s",
                this.serviceName, this.indexName, escapePathSegment(key), API_VERSION);
    }

    private String buildIndexSearchUrl(String search, IndexSearchOptions options) throws IOException {
        StringBuilder buffer = new StringBuilder();
        buffer.append(String.format("https://%s.search.windows.net/indexes/%s/docs?api-version=%s&search=%s&$count=%s",
                this.serviceName, this.indexName, API_VERSION, URLEncoder.encode(search, "UTF-8"), options.getIncludeCount()));
        if (options.getFilter() != null) {
            buffer.append("&$filter=").append(URLEncoder.encode(options.getFilter(), "UTF-8"));
        }
        if (options.getOrderby() != null) {
            buffer.append("&$orderby=").append(URLEncoder.encode(options.getOrderby(), "UTF-8"));
        }
        if (options.getSelect() != null) {
            buffer.append("&$select=").append(URLEncoder.encode(options.getSelect(), "UTF-8"));
        }
        if (options.getSearchFields() != null) {
            buffer.append("&searchFields=").append(URLEncoder.encode(options.getSearchFields(), "UTF-8"));
        }
        if (options.getFacets() != null) {
            for (String f: options.getFacets()) {
                buffer.append("&facet=").append(URLEncoder.encode(f, "UTF-8"));
            }
        }
        if (options.getHighlight() != null) {
            buffer.append("&highlight=").append(URLEncoder.encode(options.getHighlight(), "UTF-8"));
        }
        if (options.getHighlightPreTag() != null) {
            buffer.append("&highlightPreTag=").append(URLEncoder.encode(options.getHighlightPreTag(), "UTF-8"));
        }
        if (options.getHighlightPostTag() != null) {
            buffer.append("&highlightPostTag=").append(URLEncoder.encode(options.getHighlightPostTag(), "UTF-8"));
        }
        if (options.getScoringProfile() != null) {
            buffer.append("&scoringProfile=").append(URLEncoder.encode(options.getScoringProfile(), "UTF-8"));
        }
        if (options.getScoringParameters() != null) {
            for (String p: options.getScoringParameters()) {
                buffer.append("&scoringParameter=").append(URLEncoder.encode(p, "UTF-8"));
            }
        }
        if (options.getTop() != null) {
            buffer.append("&$top=").append((int)options.getTop());
        }
        if (options.getSkip() != null) {
            buffer.append("&$skip=").append((int)options.getSkip());
        }
        if (options.getRequireAllTerms()) {
            buffer.append("&searchMode=all");
        }
        if (options.getMinimumCoverage() != null) {
            buffer.append("&minimumCoverage=").append(options.getMinimumCoverage());
        }
        return buffer.toString();
    }

    private String buildIndexSuggestUrl(String search, String suggesterName, IndexSuggestOptions options) throws IOException {
        StringBuilder buffer = new StringBuilder();
        buffer.append(String.format("https://%s.search.windows.net/indexes/%s/docs/suggest?api-version=%s&search=%s&suggesterName=%s",
                this.serviceName, this.indexName, API_VERSION, URLEncoder.encode(search, "UTF-8"), suggesterName));
        if (options.getFilter() != null) {
            buffer.append("&$filter=").append(URLEncoder.encode(options.getFilter(), "UTF-8"));
        }
        if (options.getOrderby() != null) {
            buffer.append("&$orderby=").append(URLEncoder.encode(options.getOrderby(), "UTF-8"));
        }
        if (options.getSelect() != null) {
            buffer.append("&$select=").append(URLEncoder.encode(options.getSelect(), "UTF-8"));
        }
        if (options.getSearchFields() != null) {
            buffer.append("&searchFields=").append(URLEncoder.encode(options.getSearchFields(), "UTF-8"));
        }
        if (options.getHighlightPreTag() != null) {
            buffer.append("&highlightPreTag=").append(URLEncoder.encode(options.getHighlightPreTag(), "UTF-8"));
        }
        if (options.getHighlightPostTag() != null) {
            buffer.append("&highlightPostTag=").append(URLEncoder.encode(options.getHighlightPostTag(), "UTF-8"));
        }
        if (options.getFuzzy()) {
            buffer.append("&fuzzy=true");
        }
        if (options.getTop() != null) {
            buffer.append("&$top=").append((int)options.getTop());
        }
        if (options.getMinimumCoverage() != null) {
            buffer.append("&minimumCoverage=").append(options.getMinimumCoverage());
        }
        return buffer.toString();
    }

    private static String getResponseString(HttpURLConnection connection) throws IOException {
        InputStream in = connection.getResponseCode() > 399 ? connection.getErrorStream() : connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuffer buffer = new StringBuffer();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
            buffer.append("\n");
        }
        return buffer.toString();
    }

    private static String escapePathSegment(String segment) throws IOException {
        // URLEncoder.encode() is the wrong thing to use in this case, work-around with URI below
        try {
            URI uri = new URI("https", "temporary-service-name.temporary-domain.temporary-tld", "/" + segment, "");
            return uri.getPath().substring(1);
        }
        catch (URISyntaxException e) {
            throw new IOException("Invalid segment content");
        }
    }

    private static <T> T withHttpRetry(RetriableHttpOperation<T> r) throws HttpRetryException, IOException {
        final int RETRIES = 3;
        int delay = 30000; // 30 secs to start with
        HttpRetryException exception = null;
        T result = null;
        for (int i = 1; ; i++) {
            try {
                exception = null;
                result = r.run();
                break;
            } catch (HttpRetryException e) {
                if (i >= RETRIES) {
                    throw e;
                }
            }
            try {
                Thread.sleep(delay * (i + 1));
            } catch (InterruptedException e) {
                throw new IOException("Interrupted during HTTP retry", e);
            }
        }
        return result;
    }

    private static class IndexBatch {
        private Collection<IndexBatchOperation> value;

        public IndexBatch () {
            value = new ArrayList<IndexBatchOperation>();
        }

        public Collection<IndexBatchOperation> getValue() {
            return value;
        }
    }

    private interface RetriableHttpOperation<T> {
        public T run() throws HttpRetryException, IOException;
    }
}
