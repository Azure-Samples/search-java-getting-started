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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
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
        List<IndexOperation> operations = new ArrayList<>();
        Map<String, Object> catA = new HashMap<String, Object>() {{ put(ID, "A"); put(ROLE, "P1");}};
        Map<String, Object> catB = new HashMap<String, Object>() {{ put(ID, "B"); put(ROLE, "E2");}};
        Map<String, Object> catC = new HashMap<String, Object>() {{ put(ID, "C"); put(ROLE, "E1");}};
        Map<String, Object> name1 = new HashMap<String, Object>() {{ put(FIRST, "Dave"); put(LAST, "Smith");}};
        Map<String, Object> name2 = new HashMap<String, Object>() {{ put(FIRST, "David"); put(LAST, "Smith");}};
        Map<String, Object> name3 = new HashMap<String, Object>() {{ put(FIRST, "Steve"); put(LAST, "Smith");}};
        Map<String, Object> name4 = new HashMap<String, Object>() {{ put(FIRST, "Na"); put(LAST, "Smith");}};
        operations.add(IndexOperation.uploadOperation(newDocument("1", name1, 10, catA, catB)));
        operations.add(IndexOperation.uploadOperation(newDocument("2", name2, 11, catA, catC)));
        operations.add(IndexOperation.uploadOperation(newDocument("3", name3, 12, catB, catC)));
        operations.add(IndexOperation.uploadOperation(newDocument("4", name4, 13, catA)));
        operations.add(IndexOperation.deleteOperation(ID, "3"));

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

    private Map<String, Object> newDocument(String id, Map<String, Object> name, int rating, Map<String, Object>... categories) {
        Map<String, Object> doc = new HashMap<>();
        doc.put(ID, id);
        doc.put(FULL_NAME, name);
        doc.put(RATING, rating);
        doc.put(CATEGORIES, categories);
        doc.put(CREATED, new Date());
        return doc;
    }
}
