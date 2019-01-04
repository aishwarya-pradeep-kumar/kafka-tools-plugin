package com.jetbrains.kafka.tool.service.model;

public class SchemaCompatibilityResponse {
    private boolean is_compatible;

    public SchemaCompatibilityResponse(boolean is_compatible) {
        this.is_compatible = is_compatible;
    }

    public boolean isIs_compatible() {
        return this.is_compatible;
    }

    public void setIs_compatible(boolean is_compatible) {
        this.is_compatible = is_compatible;
    }
}

