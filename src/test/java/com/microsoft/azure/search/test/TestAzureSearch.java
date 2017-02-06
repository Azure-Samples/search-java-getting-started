package com.microsoft.azure.search.test;

import com.microsoft.azure.search.samples.IndexSearchOptions;
import com.microsoft.azure.search.samples.IndexSearchResult;
import com.microsoft.azure.search.samples.IndexSuggestOptions;
import com.microsoft.azure.search.samples.IndexSuggestResult;
import com.microsoft.azure.search.samples.SearchIndexClient;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

/**
 *
 * @author chris vugrinec (chvugrin@microsoft.com)
 */
public class TestAzureSearch {


    private static final String SERVICE_NAME = "YOUR SRVC NAME";
    private static final String INDEX_NAME = "sample";
    private static final String API_KEY = "YOUR API KEY";
    private static final Logger logger = Logger.getLogger("AzureSearchTest");

    SearchIndexClient indexClient = null;

    @BeforeSuite
    private void init() throws IOException, InterruptedException{
        indexClient = new SearchIndexClient(SERVICE_NAME, INDEX_NAME, API_KEY);
        TestHelper.createIndex(indexClient, true);
        TestHelper.indexData(indexClient);
        Thread.sleep(1000); //
    }

    
    @Test()
    public void testSearchSimple() throws IOException {
        logger.log(Level.INFO, "\n\ntestSearchSimple\n\n");

        IndexSearchOptions options = new IndexSearchOptions();
        options.setIncludeCount(true);
        IndexSearchResult result = indexClient.search("name", options);
        logger.log(Level.INFO, "Found {0} hits",result.getCount());
        result.getHits().forEach((hit) -> {
            logger.log(Level.INFO, "Id: {0} name {1}, score {2}",new Object[]{hit.getDocument().get("id"), hit.getDocument().get("name"), hit.getScore()});
        });
        Assert.assertNotNull(result,"Test if result not null");
        Assert.assertEquals(result.getCount(), 4,"Test if total of result is 4 records");

    }
  
    @Test()
    public void testSearchAllFeatures() throws IOException {
        logger.log(Level.INFO, "\n\ntestSearchAllFeatures\n\n");

        IndexSearchOptions options = new IndexSearchOptions();
        options.setIncludeCount(true);
        options.setFilter("rating lt 13 and category/any(c: c eq 'aaa')");
        options.setOrderby("created");
        options.setSelect("id,name,category,created");
        options.setSearchFields("name");
        options.setFacets(new String[] { "rating,values:11|13", "category", "created" });
        options.setHighlight("name");
        options.setHighlightPreTag("*pre*");
        options.setHighlightPostTag("*post*");
        options.setTop(10);
        options.setSkip(1);
        options.setRequireAllTerms(true);
        options.setMinimumCoverage(0.75);

        IndexSearchResult result = indexClient.search("second name", options);
        
        
        // list search hits
        logger.log(Level.INFO, "Found {0} hits, coverage: {1}",new Object[]{result.getCount(), result.getCoverage() == null ? "-" : result.getCoverage()});
        result.getHits().forEach((hit) -> {
            logger.log(Level.INFO, "id {0}, name: {1}, score: {2} ",new Object[]{hit.getDocument().get("id"), hit.getDocument().get("name"), hit.getScore()});
        });

        // list facets
        
        logger.log(Level.INFO, "List Facets");
        result.getFacets().keySet().stream().map((field) -> {
            logger.log(Level.INFO, "Field : ");
            return field;
        }).forEachOrdered((field) -> {
            for (IndexSearchResult.FacetValue value: result.getFacets().get(field)) {
                if (value.getValue() != null) {
                    logger.log(Level.INFO, "{0}: {1} ", new Object[]{value.getValue(), value.getCount()});
                }
                else {
                    logger.log(Level.INFO, "{0}-{1}: {2} ", new Object[]{
                        value.getFrom() == null ? "min" : value.getFrom(),
                        value.getTo() == null ? "max" : value.getTo(),
                        value.getCount()});
                }
            }
        });
        
        //  TODO:   Add more testCases
        Assert.assertNotNull(result,"Test if result not null");
        Assert.assertEquals(result.getCount(),2,"Test Expected count");
        Assert.assertEquals(result.getCoverage(),100.0,"Test Expected coverage");

    }

    @Test()
    public void testLookup() throws IOException, URISyntaxException {
        logger.log(Level.INFO, "\n\ntestLookup\n\n");

        Map<String, Object> document = indexClient.lookup("2");
        logger.log(Level.INFO, "Document lookup, key='2'");
        logger.log(Level.INFO, "Name: {0}",document.get("name"));
        logger.log(Level.INFO, "Created: {0}",document.get("created"));
        logger.log(Level.INFO, "Rating: {0}",document.get("rating"));

        //  TODO:   Add more testCases
        Assert.assertEquals(document.get("name"),"second name","Test if expected name is returned");
        Assert.assertEquals(document.get("rating"),11,"Test if expected rating is returned");

    }

    @Test()
    public void testSuggestions() throws IOException {
        logger.log(Level.INFO, "\n\ntestSuggestions\n\n");

        IndexSuggestOptions options = new IndexSuggestOptions();
        options.setFuzzy(true);
        IndexSuggestResult result = indexClient.suggest("secp", "sg", options);
        logger.log(Level.INFO, "Suggest results, coverage: {0}",(result.getCoverage() == null ? "-" : result.getCoverage().toString()));
        
        result.getHits().forEach((hit) -> {
            logger.log(Level.INFO, "Text: {0} (id: {1}",new Object[]{hit.getText(), hit.getDocument().get("id")});
        }); 

        //  TODO:   Add more testCases
        Assert.assertEquals(result.getHits().size(),2,"Test Expected hits");
    }
    
    
    
    
}
