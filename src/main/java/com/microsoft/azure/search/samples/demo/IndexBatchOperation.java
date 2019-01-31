package com.microsoft.azure.search.samples.demo;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

/*
 * Represents a batch operation in an indexing request. Only "upload" and "delete" are modeled here, there is
 * also "merge" and "mergeOrUpload" that can capture different scenarios.
 */
public abstract class IndexBatchOperation {

    public static IndexBatchOperation upload(Map<String, Object> document) {
        IndexUploadOperation operation = new IndexUploadOperation();
        operation.setDocument(document);
        return operation;
    }

    public static IndexBatchOperation delete(String keyName, String keyValue) {
        IndexDeleteOperation operation = new IndexDeleteOperation();
        operation.setKeyName(keyName);
        operation.setKeyValue(keyValue);
        return operation;
    }

    public static class IndexUploadOperation extends IndexBatchOperation {
        private Map<String, Object> document;

        public Map<String, Object> getDocument() {
            return this.document;
        }

        public void setDocument(Map<String, Object> document) {
            this.document = document;
        }

        @JsonValue
        private Map<String, Object> toJson() {
            // Copy the map to avoid side-effecting the original map
            HashMap<String, Object> map = new HashMap<String, Object>(document);
            map.put("@search.action", "upload");
            return map;
        }
    }

    public static class IndexDeleteOperation extends IndexBatchOperation {
        private String keyName;
        private String keyValue;

        public String getKeyName() {
            return keyName;
        }

        public void setKeyName(String keyName) {
            this.keyName = keyName;
        }

        public String getKeyValue() {
            return keyValue;
        }

        public void setKeyValue(String keyValue) {
            this.keyValue = keyValue;
        }

        @JsonValue
        private Map<String, Object> toJson() {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(getKeyName(), getKeyValue());
            map.put("@search.action", "delete");
            return map;
        }
    }
}
