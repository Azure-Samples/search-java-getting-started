package com.microsoft.azure.search.samples.demo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Category {
    @JsonProperty("id")
    public abstract String id();

    @JsonProperty("role")
    public abstract String role();

    @JsonCreator
    public static Category create(@JsonProperty("first") String id, @JsonProperty("last") String role) {
        return new com.microsoft.azure.search.samples.demo.AutoValue_Category(id, role);
    }
}