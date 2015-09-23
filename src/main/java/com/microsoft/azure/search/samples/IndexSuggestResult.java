package com.microsoft.azure.search.samples;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(value = { "@odata.context" })
public class IndexSuggestResult {
    private Collection<SuggestHit> hits;
    private Double coverage;

    public Collection<SuggestHit> getHits() {
        return hits;
    }

    @JsonProperty("value")
    public void setHits(Collection<SuggestHit> hits) {
        this.hits = hits;
    }

    public Double getCoverage() {
        return coverage;
    }

    @JsonProperty("@search.coverage")
    public void setCoverage(Double coverage) {
        this.coverage = coverage;
    }

    public static class SuggestHit {
        private String text;
        private Map<String, Object> document;

        @JsonCreator
        public SuggestHit(Map<String, Object> jsonMap) {
            text = (String)jsonMap.get("@search.text");
            jsonMap.remove("@search.text");
            document = jsonMap;
        }

        public String getText() {
            return text;
        }

        public Map<String, Object> getDocument() {
            return document;
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
