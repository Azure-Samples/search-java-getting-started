package com.microsoft.azure.search.samples.options;

public class SuggestOptions {
    private String filter;
    private String orderby;
    private String select;
    private String searchFields;
    private String highlightPreTag;
    private String highlightPostTag;
    private Integer top;
    private Double minimumCoverage;
    private boolean fuzzy;

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getOrderby() {
        return orderby;
    }

    public void setOrderby(String orderby) {
        this.orderby = orderby;
    }

    public String getSelect() {
        return select;
    }

    public void setSelect(String select) {
        this.select = select;
    }

    public String getSearchFields() {
        return searchFields;
    }

    public void setSearchFields(String searchFields) {
        this.searchFields = searchFields;
    }

    public String getHighlightPreTag() {
        return highlightPreTag;
    }

    public void setHighlightPreTag(String highlightPreTag) {
        this.highlightPreTag = highlightPreTag;
    }

    public String getHighlightPostTag() {
        return highlightPostTag;
    }

    public void setHighlightPostTag(String highlightPostTag) {
        this.highlightPostTag = highlightPostTag;
    }

    public boolean getFuzzy() {
        return fuzzy;
    }

    public void setFuzzy(boolean fuzzy) {
        this.fuzzy = fuzzy;
    }

    public Integer getTop() {
        return top;
    }

    public void setTop(Integer top) {
        this.top = top;
    }

    public Double getMinimumCoverage() {
        return minimumCoverage;
    }

    public void setMinimumCoverage(Double minimumCoverage) {
        this.minimumCoverage = minimumCoverage;
    }
}
