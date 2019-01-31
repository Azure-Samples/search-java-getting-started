package com.microsoft.azure.search.samples.results;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;

@JsonIgnoreProperties(value = { "@odata.context" })
public class IndexBatchResult {
    private Collection<IndexBatchOperationResult> value;
    private int status;

    public Collection<IndexBatchOperationResult> getOperationResults() {
        return value;
    }

    @JsonProperty("value")
    public void setOperationResults(Collection<IndexBatchOperationResult> value) {
        this.value = value;
    }

    public int getHttpStatus() {
        return status;
    }

    public void setHttpStatus(int status) {
        this.status = status;
    }
}
