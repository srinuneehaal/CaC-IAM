package com.cac.iam.config;

@FunctionalInterface
public interface EnvironmentLookup {

    String lookup(String key);
}
