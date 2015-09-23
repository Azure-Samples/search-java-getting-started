package com.microsoft.azure.search.samples;

import java.util.ArrayList;
import java.util.Collection;

public class IndexDefinition {
    private String name;
    private ArrayList<IndexField> fields;
    private ArrayList<Suggester> suggesters;

    public IndexDefinition() {
        fields = new ArrayList<IndexField>();
        suggesters = new ArrayList<Suggester>();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<IndexField> getFields() {
        return this.fields;
    }

    public Collection<Suggester> getSuggesters() {
        return suggesters;
    }

    public static class Suggester {
        private String name;
        private String searchMode;
        private String[] sourceFields;

        public Suggester() {
            searchMode = "analyzingInfixMatching"; // only allowed value as of version 2015-02-28
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSearchMode() {
            return searchMode;
        }

        public void setSearchMode(String searchMode) {
            this.searchMode = searchMode;
        }

        public String[] getSourceFields() {
            return sourceFields;
        }

        public void setSourceFields(String[] sourceFields) {
            this.sourceFields = sourceFields;
        }
    }
}
