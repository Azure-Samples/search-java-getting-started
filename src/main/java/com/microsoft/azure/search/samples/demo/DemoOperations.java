package com.microsoft.azure.search.samples.demo;

import com.microsoft.azure.search.samples.client.SearchIndexClient;
import com.microsoft.azure.search.samples.index.IndexDefinition;
import com.microsoft.azure.search.samples.index.IndexField;
import com.microsoft.azure.search.samples.index.Suggester;
import com.microsoft.azure.search.samples.options.SearchOptions;
import com.microsoft.azure.search.samples.options.SuggestOptions;
import com.microsoft.azure.search.samples.results.IndexBatchOperationResult;
import com.microsoft.azure.search.samples.results.IndexBatchResult;
import com.microsoft.azure.search.samples.results.SearchResult;
import com.microsoft.azure.search.samples.results.SuggestHit;
import com.microsoft.azure.search.samples.results.SuggestResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class DemoOperations {
    private static final String INDEX_NAME = "sample";
    private SearchIndexClient client;

    DemoOperations(String serviceName, String apiKey) {
        this.client = new SearchIndexClient(serviceName, INDEX_NAME, apiKey);
    }

    void createIndex() throws IOException {
        // Typical application initialization may createIndex an index if it doesn't exist. Deleting an index
        // on initialization is a sample-only thing to do
        client.deleteIndexIfExists();

        // Indexes may be created via the management UI in portal.azure.com or via APIs. In addition to field
        // details index definitions include options for custom scoring, suggesters and more
        if (!client.doesIndexExist()) {
            List<IndexField> fields = Arrays.asList(IndexField.builder("id", "Edm.String").key(true).build(),
                                                    IndexField.builder("name", "Edm.String").searchable(true)
                                                            .analyzer("en.lucene").build(),
                                                    IndexField.builder("category", "Collection(Edm.String)")
                                                            .filterable(true).facetable(true).build(),
                                                    IndexField.builder("rating", "Edm.Int32").filterable(true)
                                                            .facetable(true).build(),
                                                    IndexField.builder("created", "Edm.DateTimeOffset")
                                                            .filterable(true).sortable(true).facetable(true).build());

            Suggester suggester = Suggester.create("sg", "analyzingInfixMatching", Collections.singletonList("name"));

            client.createIndex(IndexDefinition.create(INDEX_NAME, fields, Collections.singletonList(suggester)));
        }
    }

    void indexData() throws IOException {
        // In this case we createIndex sample data in-memory. Typically this will come from another database, file or
        // API and will be turned into objects with the desired shape for indexing
        List<IndexOperation> operations = new ArrayList<>();
        operations.add(IndexOperation.uploadOperation(newDocument("1", "Dave Smith", 10, "catA", "catB")));
        operations.add(IndexOperation.uploadOperation(newDocument("2", "David Smith", 11, "catA", "catC")));
        operations.add(IndexOperation.uploadOperation(newDocument("3", "Steve Smith", 12, "catA", "catE")));
        operations.add(IndexOperation.uploadOperation(newDocument("4", "Na Smith", 13, "catD", "catE")));
        operations.add(IndexOperation.deleteOperation("id", "4"));

        IndexBatchResult result = client.indexBatch(operations);
        if (result.status() != null && result.status() ==  207) {
            System.out.print("handle partial success, check individual client status/error message");
        }
        for (IndexBatchOperationResult r : result.value()) {
            System.out.printf("Operation for id: %s, success: %s\n", r.key(), r.status());
        }
    }

    void searchSimple() throws IOException {
        SearchOptions options = SearchOptions.builder().includeCount(true).build();
        SearchResult result = client.search("Smith", options);
        System.out.printf("Found %s hits\n", result.count());
        for (SearchResult.SearchHit hit : result.hits()) {
            System.out.printf("\tid: %s, name: %s, score: %s\n", hit.document().get("id"),
                              hit.document().get("name"), hit.score());
        }
    }

    void searchAllFeatures() throws IOException {
        SearchOptions options = SearchOptions.builder()
                .includeCount(true)
                .filter("rating lt 13 and category/any(c: c eq 'catA')")
                .orderBy("created")
                .select("id,name,category,created")
                .searchFields("name")
                .facets(Arrays.asList("rating,values:11|13", "category", "created"))
                .highlight("name")
                .highlightPreTag("*pre*")
                .highlightPostTag("*post*")
                .top(10)
                .skip(1)
                .requireAllTerms(true)
                .minimumCoverage(0.75).build();

        SearchResult result = client.search("Dave", options);

        // list search hits
        System.out.printf("Found %s hits, coverage: %s\n", result.count(),
                          result.coverage() == null ? "-" : result.coverage());
        for (SearchResult.SearchHit hit : result.hits()) {
            System.out.printf("\tid: %s, name: %s, score: %s\n", hit.document().get("id"),
                              hit.document().get("name"), hit.score());
        }

        // list facets
        for (String field : Objects.requireNonNull(result.facets()).keySet()) {
            System.out.println(field + ":");
            for (SearchResult.FacetValue value : Objects.requireNonNull(result.facets()).get(field)) {
                if (value.value() != null) {
                    System.out.printf("\t%s: %s\n", value.value(), value.count());
                } else {
                    System.out.printf("\t%s-%s: %s\n", value.from() == null ? "min" : value.from(),
                                      value.to() == null ? "max" : value.to(), value.count());
                }
            }
        }
    }

    void lookup() throws IOException {
        Map<String, Object> document = client.lookup("2");
        System.out.println("Document lookup, key='2'");
        System.out.printf("\tname: %s\n", document.get("name"));
        System.out.printf("\tcreated: %s\n", document.get("created"));
        System.out.printf("\trating: %s\n", document.get("rating"));
    }

    void suggest() throws IOException {
        SuggestOptions options = SuggestOptions.builder().fuzzy(true).build();
        SuggestResult result = client.suggest("dap", "sg", options);
        System.out.println("Suggest results, coverage: " + result.coverage());
        for (SuggestHit hit : result.hits()) {
            System.out.printf("\ttext: %s (id: %s)\n", hit.text(), hit.document().get("id"));
        }
    }

    private Map<String, Object> newDocument(String id, String name, int rating, String... categories) {
        HashMap<String, Object> doc = new HashMap<>();
        doc.put("id", id);
        doc.put("name", name);
        doc.put("rating", rating);
        doc.put("category", categories);
        doc.put("created", new Date());
        return doc;
    }
}
