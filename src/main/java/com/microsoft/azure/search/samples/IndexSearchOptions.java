package com.microsoft.azure.search.samples;

public class IndexSearchOptions {
    private boolean includeCount;
    private String filter;
    private String orderby;
    private String select;
    private String searchFields;
    private String[] facets;
    private String highlight;
    private String highlightPreTag;
    private String highlightPostTag;
    private String scoringProfile;
    private String[] scoringParameters;
    private Integer top;
    private Integer skip;
    private boolean requireAllTerms;
    private Double minimumCoverage;

    public boolean getIncludeCount() {
        return includeCount;
    }

    public void setIncludeCount(boolean includeCount) {
        this.includeCount = includeCount;
    }

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

    public String[] getFacets() {
        return facets;
    }

    public void setFacets(String[] facets) {
        this.facets = facets;
    }

    public String getHighlight() {
        return highlight;
    }

    public void setHighlight(String highlight) {
        this.highlight = highlight;
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

    public String getScoringProfile() {
        return scoringProfile;
    }

    public void setScoringProfile(String scoringProfile) {
        this.scoringProfile = scoringProfile;
    }

    public String[] getScoringParameters() {
        return scoringParameters;
    }

    public void setScoringParameters(String[] scoringParameters) {
        this.scoringParameters = scoringParameters;
    }

    public Integer getTop() {
        return top;
    }

    public void setTop(Integer top) {
        this.top = top;
    }

    public Integer getSkip() {
        return skip;
    }

    public void setSkip(Integer skip) {
        this.skip = skip;
    }

    public boolean getRequireAllTerms() {
        return requireAllTerms;
    }

    public void setRequireAllTerms(boolean requireAllTerms) {
        this.requireAllTerms = requireAllTerms;
    }

    public Double getMinimumCoverage() {
        return minimumCoverage;
    }

    public void setMinimumCoverage(Double minimumCoverage) {
        this.minimumCoverage = minimumCoverage;
    }
}
