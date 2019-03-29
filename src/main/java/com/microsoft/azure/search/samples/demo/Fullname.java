package com.microsoft.azure.search.samples.demo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Fullname {
    @JsonProperty("first")
    public abstract String first();
    @JsonProperty("last")
    public abstract String last();

    @JsonCreator
    public static Fullname create(@JsonProperty("first") String first, @JsonProperty("last") String last) {
        return new com.microsoft.azure.search.samples.demo.AutoValue_Fullname(first, last);
    }
}
