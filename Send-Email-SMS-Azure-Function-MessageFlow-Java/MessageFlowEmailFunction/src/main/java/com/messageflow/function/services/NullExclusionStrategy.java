package com.messageflow.function.services;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class NullExclusionStrategy implements ExclusionStrategy {
    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        return false;
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }
}
