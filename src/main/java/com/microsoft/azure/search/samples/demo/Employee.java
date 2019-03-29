package com.microsoft.azure.search.samples.demo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class Employee {
    @JsonProperty("id")
    public abstract String id();

    @JsonProperty("fullname")
    public abstract Fullname fullname();

    @JsonProperty("rating")
    public abstract int rating();

    @JsonProperty("created")
    public abstract String created();

    @JsonProperty("categories")
    public abstract List<Category> categories();

    @JsonCreator
    public static Employee create(@JsonProperty("id") String id, @JsonProperty("fullname") Fullname fullname,
            @JsonProperty("rating") int rating, @JsonProperty("created") String created,
            @JsonProperty("categories") List<Category> categories) {
        return new com.microsoft.azure.search.samples.demo.AutoValue_Employee(id, fullname, rating, created,
                                                                              categories);
    }
}
