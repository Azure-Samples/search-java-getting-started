package com.microsoft.azure.search.samples.results;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(value = { "@odata.context" })
public class IndexSearchResult {
    private Collection<SearchHit> hits;
    private long count;
    private String nextLink;
    private Double coverage;
    private FacetsResult facets;

    public IndexSearchResult() {
        count = -1;
    }

    public Collection<SearchHit> getHits() {
        return hits;
    }

    @JsonProperty("value")
    public void setHits(Collection<SearchHit> hits) {
        this.hits = hits;
    }

    public long getCount() {
        return count;
    }

    @JsonProperty("@odata.count")
    public void setCount(long count) {
        this.count = count;
    }

    public String getNextLink() {
        return nextLink;
    }

    @JsonProperty("@odata.nextLink")
    public void setNextLink(String nextLink) {
        this.nextLink = nextLink;
    }

    public Double getCoverage() {
        return coverage;
    }

    @JsonProperty("@search.coverage")
    public void setCoverage(Double coverage) {
        this.coverage = coverage;
    }

    public FacetsResult getFacets() {
        return facets;
    }

    @JsonProperty("@search.facets")
    public void setFacets(FacetsResult facets) {
        this.facets = facets;
    }

    public static class SearchHit {
        private Map<String, Object> document;
        private Map<String, String[]> highlights;
        private double score;

        @JsonCreator
        public SearchHit(Map<String, Object> jsonMap) {
            score = (Double)jsonMap.get("@search.score");
            jsonMap.remove("@search.score");
            if (jsonMap.containsKey("@search.highlights")) {
                highlights = (Map<String, String[]>)jsonMap.get("@search.highlights");
                ArrayList<String> keys = new ArrayList<String>();
                for (String k: highlights.keySet()) {
                    if (k.endsWith("@odata.type")) {
                        keys.add(k);
                    }
                }
                for (String k: keys) {
                    highlights.remove(k);
                }
                jsonMap.remove("@search.highlights");
            }
            document = jsonMap;
        }

        public double getScore() {
            return score;
        }

        public Map<String, Object> getDocument() {
            return document;
        }

        public Map<String, String[]> getHighlights() {
            return highlights;
        }
    }

    public static class FacetsResult extends HashMap<String, FacetValue[]> {
        @JsonCreator
        public FacetsResult(Map<String, Object> jsonMap) {
            // jackson doesn't like mixed strings/objects in the facet object, so we manually unpack it here
            for(String field: jsonMap.keySet()) {
                if (!field.endsWith("@odata.type")) {
                    Collection<Map<String, Object>> values = (Collection<Map<String, Object>>)jsonMap.get(field);
                    FacetValue[] valueList = new FacetValue[values.size()];
                    int i = 0;
                    for (Map<String, Object> value: values) {
                        FacetValue facet = new FacetValue();
                        facet.setCount((Integer)value.get("count"));
                        if (value.containsKey("value")) {
                            facet.setValue(value.get("value"));
                        }
                        if (value.containsKey("from")) {
                            facet.setFrom(value.get("from"));
                        }
                        if (value.containsKey("to")) {
                            facet.setTo(value.get("to"));
                        }
                        valueList[i++] = facet;
                    }
                    this.put(field, valueList);
                }
            }
        }
    }

    public static class FacetValue {
        private Object value;
        private Object from;
        private Object to;
        private long count;

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public Object getFrom() {
            return from;
        }

        public void setFrom(Object from) {
            this.from = from;
        }

        public Object getTo() {
            return to;
        }

        public void setTo(Object to) {
            this.to = to;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }
    }
}
