package com.microsoft.azure.search.samples.demo;

import com.microsoft.azure.search.samples.client.SearchIndexClient;
import com.microsoft.azure.search.samples.index.ComplexIndexField;
import com.microsoft.azure.search.samples.index.IndexDefinition;
import com.microsoft.azure.search.samples.index.IndexField;
import com.microsoft.azure.search.samples.index.SimpleIndexField;
import com.microsoft.azure.search.samples.index.Suggester;
import com.microsoft.azure.search.samples.options.SearchOptions;
import com.microsoft.azure.search.samples.options.SuggestOptions;
import com.microsoft.azure.search.samples.results.IndexBatchOperationResult;
import com.microsoft.azure.search.samples.results.IndexBatchResult;
import com.microsoft.azure.search.samples.results.SearchResult;
import com.microsoft.azure.search.samples.results.SuggestHit;
import com.microsoft.azure.search.samples.results.SuggestResult;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/* The following sample document illustrates the schema of indexed documents
   [
        {
            "id": "2",
            "rating": 11,
            "created": "[TIME]",
            "fullname": {
                "first": "David",
                "last": "Smith"
            },
            "categories": [
                {
                    "id": "A",
                    "role": "P1"
                },
                {
                    "id": "C",
                    "role": "E1"
                }
            ]
        }
    ]
 */
class DemoOperations {
    private static final String INDEX_NAME = "sample";
    private static final String ID = "id";
    private static final String ROLE = "role";
    private static final String FIRST = "first";
    private static final String LAST = "last";
    private static final String FULL_NAME = "fullname";
    private static final String CATEGORIES = "categories";
    private static final String CREATED = "created";
    private static final String RATING = "rating";
    private SearchIndexClient client;

    DemoOperations(String serviceName, String apiKey) {
        this.client = new SearchIndexClient(serviceName, INDEX_NAME, apiKey);
    }

    // Indexes may be created via the management UI in portal.azure.com or via APIs. In addition to field
    // details index definitions include options for custom scoring, suggesters and more
    void createIndex() throws IOException {
        // Typical application initialization may createIndex an index if it doesn't exist. Deleting an index
        // on initialization is a sample-only thing to do
        client.deleteIndexIfExists();

        if (!client.doesIndexExist()) {
            IndexField fullName = ComplexIndexField.create(FULL_NAME,
                                                           Arrays.asList(
                                                                       SimpleIndexField.builder(FIRST, "Edm.String")
                                                                               .searchable(true)
                                                                               .analyzer("en.lucene")
                                                                               .build(),
                                                                       SimpleIndexField.builder(LAST, "Edm.String")
                                                                               .searchable(true)
                                                                               .analyzer("en.lucene")
                                                                               .build()
                                                               ),
                                                           false);
            IndexField categories = ComplexIndexField.create(CATEGORIES,
                                                             Arrays.asList(
                                                                     SimpleIndexField.builder(ID, "Edm.String")
                                                                             .filterable(true).facetable(true)
                                                                             .build(),
                                                                     SimpleIndexField.builder(ROLE, "Edm.String")
                                                                             .build()
                                                             ),
                                                             true);
            List<IndexField> fields = Arrays.asList(SimpleIndexField.builder(ID, "Edm.String").key(true).build(),
                                                    fullName,
                                                    categories,
                                                    SimpleIndexField.builder(RATING, "Edm.Int32").filterable(true)
                                                            .facetable(true).build(),
                                                    SimpleIndexField.builder(CREATED, "Edm.DateTimeOffset")
                                                            .filterable(true).sortable(true).facetable(true).build());

            Suggester suggester = Suggester.create("sg", "analyzingInfixMatching", Collections.singletonList
                    (FULL_NAME + "/" + FIRST));

            client.createIndex(IndexDefinition.create(INDEX_NAME, fields, Collections.singletonList(suggester)));
        }
    }

    void indexData() throws IOException {
        // In this case we createIndex sample data in-memory. Typically this will come from another database, file or
        // API and will be turned into objects with the desired shape for indexing
        Category cA = Category.create("A", "P1");
        Category cB = Category.create("B", "E2");
        Category cC = Category.create("C", "E1");

        Fullname n1 = Fullname.create("Dave", "Smith");
        Fullname n2 = Fullname.create("David", "Smith");
        Fullname n3 = Fullname.create("Steve", "Smith");
        Fullname n4 = Fullname.create("Na", "Smith");

        String time = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        Employee e1 = Employee.create("1", n1, 10, time, Arrays.asList(cA, cB));
        Employee e2 = Employee.create("2", n2, 10, time, Arrays.asList(cA, cC));
        Employee e3 = Employee.create("3", n3, 10, time, Arrays.asList(cB, cC));
        Employee e4 = Employee.create("4", n4, 10, time, Collections.singletonList(cA));

        List<IndexOperation> ops = new ArrayList<>();
        ops.add(IndexOperation.uploadOperation(e1));
        ops.add(IndexOperation.uploadOperation(e2));
        ops.add(IndexOperation.uploadOperation(e3));
        ops.add(IndexOperation.uploadOperation(e4));
        ops.add(IndexOperation.deleteOperation(ID, "3"));

        IndexBatchResult result = client.indexBatch(ops);
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
            System.out.printf("\tid: %s, name: %s, score: %s\n", hit.document().get(ID),
                              hit.document().get(FULL_NAME), hit.score());
        }
    }

    void searchAllFeatures() throws IOException {
        SearchOptions options = SearchOptions.builder()
                .includeCount(true)
                .filter("rating lt 13 and categories/any(c: c/id eq 'A')")
                .orderBy(CREATED)
                .select("id,fullname,categories,created")
                .searchFields(FULL_NAME + "/last")
                .facets(Arrays.asList("rating,values:11|13", "categories/id", CREATED))
                .highlight(FULL_NAME + "/last")
                .highlightPreTag("*pre*")
                .highlightPostTag("*post*")
                .top(10)
                .skip(1)
                .requireAllTerms(true)
                .minimumCoverage(0.75).build();

        SearchResult result = client.search("Smith", options);

        // list search hits
        System.out.printf("Found %s hits, coverage: %s\n", result.count(),
                          result.coverage() == null ? "-" : result.coverage());
        for (SearchResult.SearchHit hit : result.hits()) {
            System.out.printf("\tid: %s, name: %s, score: %s\n", hit.document().get(ID),
                              hit.document().get(FULL_NAME), hit.score());
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
        System.out.printf("\tname: %s\n", document.get(FULL_NAME));
        System.out.printf("\tcreated: %s\n", document.get(CREATED));
        System.out.printf("\trating: %s\n", document.get(RATING));
    }

    void suggest() throws IOException {
        SuggestOptions options = SuggestOptions.builder().fuzzy(true).build();
        SuggestResult result = client.suggest("dap", "sg", options);
        System.out.println("Suggest results, coverage: " + result.coverage());
        for (SuggestHit hit : result.hits()) {
            System.out.printf("\ttext: %s (id: %s)\n", hit.text(), hit.document().get(ID));
        }
    }
}
