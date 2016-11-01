package com.microsoft.azure.search.test;

import com.microsoft.azure.search.samples.IndexBatchOperation;
import com.microsoft.azure.search.samples.IndexBatchResult;
import com.microsoft.azure.search.samples.IndexDefinition;
import com.microsoft.azure.search.samples.IndexField;
import com.microsoft.azure.search.samples.SearchIndexClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author chris vugrinec (chvugrin@microsoft.com)
 */
public class TestHelper {

    private static final Logger logger = Logger.getLogger("TestHelper");

    static void createIndex(SearchIndexClient indexClient, boolean deleteFirst) throws IOException {
        // Typical application initialization may create an index if it doesn't exist. Deleting an index
        // on initialization is a sample-only thing to do
        if (deleteFirst) {
            indexClient.delete();
        }

        // Indexes may be created via the management UI in portal.azure.com or via APIs. In addition to field
        // details index definitions include options for custom scoring, suggesters and more
        if (!indexClient.exists()) {
            IndexDefinition definition = new IndexDefinition();

            Collection<IndexField> fields = definition.getFields();
            fields.add(new IndexField("id", "Edm.String").setKey(true));
            fields.add(new IndexField("name", "Edm.String").setSearchable(true).setAnalyzer("en.lucene"));
            fields.add(new IndexField("category", "Collection(Edm.String)").setFilterable(true).setFacetable(true));
            fields.add(new IndexField("rating", "Edm.Int32").setFilterable(true).setFacetable(true));
            fields.add(new IndexField("created", "Edm.DateTimeOffset").setFilterable(true).setSortable(true).setFacetable(true));

            IndexDefinition.Suggester suggester = new IndexDefinition.Suggester();
            suggester.setName("sg");
            suggester.setSourceFields(new String[]{"name"});
            definition.getSuggesters().add(suggester);

            indexClient.create(definition);
        }
    }

    static void indexData(SearchIndexClient indexClient) throws IOException {
        // In this case we create sample data in-memory. Typically this will come from another database, file or
        // API and will be turned into objects with the desired shape for indexing
        ArrayList<IndexBatchOperation> operations = new ArrayList<>();
        operations.add(IndexBatchOperation.upload(newDocument("1", "first name", 10, "aaa", "bbb")));
        operations.add(IndexBatchOperation.upload(newDocument("2", "second name", 11, "aaa", "ccc")));
        operations.add(IndexBatchOperation.upload(newDocument("3", "second second name", 12, "aaa", "eee")));
        operations.add(IndexBatchOperation.upload(newDocument("4", "third name", 13, "ddd", "eee")));
        operations.add(IndexBatchOperation.delete("id", "5"));

        // consider handling HttpRetryException and backoff (wait 30, 60, 90 seconds) and retry
        IndexBatchResult result = indexClient.indexBatch(operations);
        if (result.getHttpStatus() == 207) {
            // handle partial success, check individual operation status/error message
        }
        result.getOperationResults().forEach((r) -> {
            logger.log(Level.INFO, "Operation for id: {0}, success: {1}  ",new Object[]{r.getKey(), r.getStatus()});
        });
    }
    
    private static Map<String, Object> newDocument(String id, String name, int rating, String ... categories) {
        HashMap<String, Object> doc;
        doc = new HashMap<>();
        doc.put("id", id);
        doc.put("name", name);
        doc.put("rating", rating);
        doc.put("category", (String[])categories);
        doc.put("created", new Date());
        return doc;
    }


}
