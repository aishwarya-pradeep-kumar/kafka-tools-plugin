package com.jetbrains.kafka.tool.service.model;

import java.time.Instant;

public class ProducerHistoryEntry {
    private String id;
    private String topic;
    private Class serializerClass;
    private String schema;
    private String key;
    private String payload;
    private String timestamp;

    public ProducerHistoryEntry() {
        this.id = "id";
        this.topic = "topic";
        this.serializerClass = Class.class;
        this.schema = "schema";
        this.key = "key";
        this.payload = "payload";
        this.timestamp = Instant.now().toString();
    }

    public ProducerHistoryEntry(String id, String topic, Class serializerClass, String schema, String key, String payload, String timestamp) {
        this.id = id;
        this.topic = topic;
        this.serializerClass = serializerClass;
        this.schema = schema;
        this.key = key;
        this.payload = payload;
        this.timestamp = timestamp;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTopic() {
        return this.topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Class getSerializerClass() {
        return this.serializerClass;
    }

    public void setSerializerClass(Class serializerClass) {
        this.serializerClass = serializerClass;
    }

    public String getSchema() {
        return this.schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPayload() {
        return this.payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        sb.append("topic='").append(this.topic).append('\'');
        sb.append(", timestamp='").append(this.timestamp).append('\'');
        sb.append(", serializerClass=").append(this.serializerClass);
        sb.append(", key='").append(this.key).append('\'');
        sb.append(", payload='").append(this.payload).append('\'');
        sb.append(", schema='").append(this.schema).append('\'');
        sb.append(", id='").append(this.id).append('\'');
        sb.append('}');
        return sb.toString();
    }
}