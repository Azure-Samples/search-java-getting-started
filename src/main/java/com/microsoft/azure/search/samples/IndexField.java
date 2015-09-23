package com.microsoft.azure.search.samples;

public class IndexField {
    private String name;
    private String type;
    private String analyzer;
    private boolean searchable;
    private boolean filterable;
    private boolean retrievable;
    private boolean sortable;
    private boolean facetable;
    private boolean key;

    public IndexField() {
        this.retrievable = true;
    }

    public IndexField(String name, String type) {
        this();
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public IndexField setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public IndexField setType(String type) {
        this.type = type;
        return this;
    }

    public String getAnalyzer() {
        return analyzer;
    }

    public IndexField setAnalyzer(String analyzer) {
        this.analyzer = analyzer;
        return this;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public IndexField setSearchable(boolean searchable) {
        this.searchable = searchable;
        return this;
    }

    public boolean isFilterable() {
        return filterable;
    }

    public IndexField setFilterable(boolean filterable) {
        this.filterable = filterable;
        return this;
    }

    public boolean isRetrievable() {
        return retrievable;
    }

    public IndexField setRetrievable(boolean retrievable) {
        this.retrievable = retrievable;
        return this;
    }

    public boolean isSortable() {
        return sortable;
    }

    public IndexField setSortable(boolean sortable) {
        this.sortable = sortable;
        return this;
    }

    public boolean isFacetable() {
        return facetable;
    }

    public IndexField setFacetable(boolean facetable) {
        this.facetable = facetable;
        return this;
    }

    public boolean isKey() {
        return key;
    }

    public IndexField setKey(boolean key) {
        this.key = key;
        return this;
    }
}
