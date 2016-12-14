package com.microsoft.azure.search.samples;

public class IndexBatchOperationResult {
    private String key;
    private boolean status;
    private String errorMessage;
    private int statusCode;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public void setStatusCode(int status) { 
        this.statusCode = statusCode; 
    }
}
