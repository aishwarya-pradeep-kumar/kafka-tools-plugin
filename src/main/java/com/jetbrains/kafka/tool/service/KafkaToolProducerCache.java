package com.jetbrains.kafka.tool.service;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

@State(
        name = "KafkaToolProducerCache",
        storages = {@Storage("kafka-tool-state.xml")}
)
public class KafkaToolProducerCache implements PersistentStateComponent<KafkaToolProducerCache> {
    private String serializerKey;
    private String topic;
    private String schema;
    private String key;
    private String payload;
    private String selectedAvroClass;

    public KafkaToolProducerCache() {
    }

    @Nullable
    public KafkaToolProducerCache getState() {
        return this;
    }

    public void loadState(KafkaToolProducerCache kafkaToolProducerCache) {
        XmlSerializerUtil.copyBean(kafkaToolProducerCache, this);
    }

    public String getSerializerKey() {
        return this.serializerKey;
    }

    public void setSerializerKey(String serializerKey) {
        this.serializerKey = serializerKey;
    }

    public String getTopic() {
        return this.topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
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

    public String getSelectedAvroClass() {
        return this.selectedAvroClass;
    }

    public void setSelectedAvroClass(String selectedAvroClass) {
        this.selectedAvroClass = selectedAvroClass;
    }
}

